import pyhdb, os.path, argparse

class DotExport(object):

    def __init__(self, host, port, user, password, workspace_schema_name, workspace_name, connection=None):
        self.bulk_fetch_size = 10
        self.connection = connection
        if not connection:
            self.connection = pyhdb.connect(host=host, port=port, user=user, password=password)
        self.workspace_schema_name = workspace_schema_name
        self.workspace_name = workspace_name

    def _wrap(self, text, wrap='"'):
        return wrap + str(text) + wrap

    def _get_workspace_data(self):
        workspace_data_stmt_id = self.cursor.prepare("SELECT VERTEX_SCHEMA_NAME, VERTEX_TABLE_NAME, VERTEX_KEY_COLUMN_NAME, EDGE_SCHEMA_NAME, EDGE_TABLE_NAME, EDGE_SOURCE_COLUMN_NAME, EDGE_TARGET_COLUMN_NAME, EDGE_KEY_COLUMN_NAME, IS_VALID FROM GRAPH_WORKSPACES WHERE SCHEMA_NAME = ? AND WORKSPACE_NAME = ?")
        self.cursor.execute_prepared(self.cursor.get_prepared_statement(workspace_data_stmt_id), [[self.workspace_schema_name, self.workspace_name]])
        result = self.cursor.fetchone()

        if not result or result[8] != 'TRUE': raise Exception("Graph workspace not found or invalid")
        return result

    def _write_node_data(self, file_handle):
        self.cursor.execute("SELECT * FROM " + self._wrap(self.workspace_data[0]) + "." + self._wrap(self.workspace_data[1]))
        vertex_data = self.cursor.fetchmany(self.bulk_fetch_size)
        vertex_columns = vertex_columns = [column[0] for column in self.cursor.description]
        
        while vertex_data:
            for row_index, row in enumerate(vertex_data):
                vertex_attributes = []
                label_attribute = None
                for col_index, column_value in enumerate(row):
                    if vertex_columns[col_index] == self.workspace_data[2]:
                        label_attribute = "label=" + self._wrap(column_value)
                        self.id_map[str(column_value)] = "node_" + str(row_index)
                    vertex_attributes.append(self._wrap(vertex_columns[col_index], "'") + "=" + self._wrap(column_value, "'"))
                file_handle.write("\tnode_" + str(row_index) + " [" + label_attribute + ", __custom_attributes=" + self._wrap(", ".join(vertex_attributes)) + "]\n")
            vertex_data = self.cursor.fetchmany(self.bulk_fetch_size)

    def _write_edge_data(self, file_handle):
        self.cursor.execute('SELECT * FROM "' + self.workspace_data[3] + '"."' + self.workspace_data[4] + '"')
        edge_data = self.cursor.fetchmany(self.bulk_fetch_size)
        edge_columns = [column[0] for column in self.cursor.description]
        while edge_data:
            for row in edge_data:
                edge_attributes = []
                for col_index, column_value in enumerate(row):
                    if edge_columns[col_index] == self.workspace_data[5]: src = self.id_map[str(column_value)]
                    if edge_columns[col_index] == self.workspace_data[6]: target = self.id_map[str(column_value)]
                    edge_attributes.append(self._wrap(edge_columns[col_index], "'") + "=" + self._wrap(column_value, "'"))
                file_handle.write("\t" + src + " -> " + target + " [__custom_attributes=" + self._wrap(", ".join(edge_attributes)) + ']\n')
            edge_data = self.cursor.fetchmany(self.bulk_fetch_size)

    def export(self, file_path):
        self.cursor = self.connection.cursor()
        self.workspace_data = self._get_workspace_data()
        self.id_map = {}

        with open(file_path, 'w+') as file_handle:
            file_handle.write('strict digraph {' + "\n")
            self._write_node_data(file_handle)
            self._write_edge_data(file_handle)
            file_handle.write('}')
        
        self.cursor.close()

    def close(self):
        self.connection.close()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("-dh", "--db_host", help="Host of the SAP HANA database", required=True)
    parser.add_argument("-dp", "--db_port", help="SQL port of the SAP HANA database", required=True)
    parser.add_argument("-du", "--db_user", help="Database user with access to the graph workspace", required=True)
    parser.add_argument("-dpw", "--db_password", help="Password of the database user", required=True)
    parser.add_argument("-dws", "--db_workspace_schema", help="Graph workspace schema name", required=True)
    parser.add_argument("-dwn", "--db_workspace_name", help="Graph workspace name", required=True)
    parser.add_argument("-f", "--file", help=" Output file", required=True)
    args = parser.parse_args()

    export = DotExport(user=args.db_user, password=args.db_password, host=args.db_host, port=args.db_port, workspace_name=args.db_workspace_name, workspace_schema_name=args.db_workspace_schema)
    export.export(file_path=args.file)
    export.close()

if __name__ == "__main__":
    main()