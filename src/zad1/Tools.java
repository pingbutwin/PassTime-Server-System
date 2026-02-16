package zad1;


import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Tools {
    static Options createOptionsFromYaml(String fileName) throws Exception {
        InputStream is = new FileInputStream(fileName);
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap = yaml.load(is);

        return new Options(String.valueOf(yamlMap.get("host")), (int)yamlMap.get("port"),
                (boolean)yamlMap.get("concurMode"), (boolean)yamlMap.get("showSendRes"),
                (Map<String, List<String>>)yamlMap.get("clientsMap"));
    }
}
