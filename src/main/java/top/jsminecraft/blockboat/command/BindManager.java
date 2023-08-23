package top.jsminecraft.blockboat.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BindManager {
    private static final File FILE = new File("config/blockboat-bind.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private Map<String, String> idToNameMap = new HashMap<>();
    private Map<String, String> nameToIdMap = new HashMap<>();

    @SneakyThrows
    public BindManager() {
        if (FILE.exists()) {
            try {
                Map<?, ?> map = mapper.readValue(FILE, Map.class);
                idToNameMap = (Map<String, String>) map.get("idToNameMap");
                nameToIdMap = (Map<String, String>) map.get("nameToIdMap");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else FILE.createNewFile();
    }

    public boolean bind(String id, String name) {
        if (idToNameMap.containsKey(id)) {
            return false;
        }
        idToNameMap.put(id, name);
        nameToIdMap.put(name, id);
        saveToFile();
        return true;
    }

    public boolean unbindById(String id) {
        if (!idToNameMap.containsKey(id)) {
            return false;
        }
        String name = idToNameMap.get(id);
        idToNameMap.remove(id);
        nameToIdMap.remove(name);
        saveToFile();
        return true;
    }

    public boolean unbindByName(String name) {
        if (!nameToIdMap.containsKey(name)) {
            return false;
        }
        String id = nameToIdMap.get(name);
        nameToIdMap.remove(name);
        idToNameMap.remove(id);
        saveToFile();
        return true;
    }

    public boolean IsIdBind(String id) {
        return idToNameMap.containsKey(id);
    }

    public String getNameById(String id) {
        return idToNameMap.get(id);
    }

    public String printAll() {
        StringBuilder Out = new StringBuilder("ID\tName\n");
        for (String id : idToNameMap.keySet()) {
            String name = idToNameMap.get(id);
            Out.append(id).append("\t").append(name).append("\n");
        }
        return Out.toString();
    }

    private void saveToFile() {
        Map<String, Object> map = new HashMap<>();
        map.put("idToNameMap", idToNameMap);
        map.put("nameToIdMap", nameToIdMap);
        try {
            mapper.writeValue(FILE, map);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
