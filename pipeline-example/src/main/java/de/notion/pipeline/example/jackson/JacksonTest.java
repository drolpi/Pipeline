package de.notion.pipeline.example.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.util.Map;

public class JacksonTest {

    public static void main(String[] args) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        Gson gson = new Gson();
        
        Person person = new Person("Peter", 10, new Heart(50));

        long jStartTime = System.currentTimeMillis();

        Map<String, Object> objectMap = objectMapper.convertValue(person, Map.class);

        System.out.println(System.currentTimeMillis() - jStartTime + "ms");

        print(objectMap);

        long gStartTime = System.currentTimeMillis();

        String objectString = gson.toJson(person);

        long middleTime = System.currentTimeMillis();
        System.out.println(middleTime - gStartTime + "ms");

        Map<String, Object> gobjectMap = gson.fromJson(objectString, Map.class);

        System.out.println(System.currentTimeMillis() - gStartTime + "ms");

        print(gobjectMap);
    }

    private static void print(Map<String, Object> objectMap) {
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            if(entry.getValue() instanceof Map) {
                print((Map<String, Object>) entry.getValue());
                continue;
            }
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

}
