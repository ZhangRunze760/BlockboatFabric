package fun.jsserver.blockboat.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

//机器人的绑定管理。整体原理非常简单，不过多赘述。
class Person {
    String name;
    String id;

    Person(String name, String id) {
        this.name = name;
        this.id = id;
    }
}

public class BindManager {
    private final Map<String, Person> nameToPersonMap;
    private final Map<String, Person> idToPersonMap;
    private final String jsonFilePath;
    private final Gson gson;

    public BindManager(String jsonFilePath) {
        File FILE = new File(jsonFilePath);
        if (!FILE.exists()) {
            try {
                FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.jsonFilePath = jsonFilePath;
        this.nameToPersonMap = new HashMap<>();
        this.idToPersonMap = new HashMap<>();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadFromFile();
    }

    public boolean bind(String name, String id) {
        if (isBindById(id)) return false;
        else {
            Person person = new Person(name, id);
            nameToPersonMap.put(name, person);
            idToPersonMap.put(id, person);
            saveToFile();
            return true;
        }
    }

    public void unbindByName(String name) {
        Person person = nameToPersonMap.remove(name);
        if (person != null) {
            idToPersonMap.remove(person.id);
            saveToFile();
        }
    }

    public boolean unbindById(String id) {
        Person person = idToPersonMap.remove(id);
        if (person != null) {
            nameToPersonMap.remove(person.name);
            saveToFile();
            return true;
        }
        else return false;
    }

    public String findIdByName(String name) {
        Person person = nameToPersonMap.get(name);
        return person.id;
    }

    public String findNameById(String id) {
        Person person = idToPersonMap.get(id);
        return person.name;
    }

    public boolean isBindById(String id) {
        return idToPersonMap.containsKey(id);
    }

    public boolean isBindByName(String name) {
        return nameToPersonMap.containsKey(name);
    }

    private void loadFromFile() {
        try (Reader reader = new FileReader(jsonFilePath)) {
            Person[] people = gson.fromJson(reader, Person[].class);
            if (people != null) {
                for (Person person : people) {
                    nameToPersonMap.put(person.name, person);
                    idToPersonMap.put(person.id, person);
                }
            }
        } catch (IOException e) {
            // File doesn't exist or couldn't be read
        }
    }

    private void saveToFile() {
        try (Writer writer = new FileWriter(jsonFilePath)) {
            gson.toJson(nameToPersonMap.values(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}