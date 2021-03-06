package ru.otus.gsom.converters;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import lombok.SneakyThrows;
import ru.otus.gsom.MyGson;

public class Converters {

  public static final Converter<String> STRING_CONVERTER = new ConverterBase<>(
      (object, myGson) -> myGson.getJsonProvider().createValue(object),
      String.class);

  public static final Converter<Character> CHARACTER_CONVERTER = new ConverterBase<>(
      (object, myGson) -> myGson.getJsonProvider().createValue(object.toString()),
      Character.class);

  public static final Converter<Number> NUMBER_CONVERTER = new ConverterBase<>(
      (object, myGson) ->
      {
        var jsonProvider = myGson.getJsonProvider();

        //т.к. createValue принимаем минимум int
        JsonNumber jsonProviderValue;
        if (object instanceof Byte || object instanceof Short || object instanceof Integer) {
          jsonProviderValue = jsonProvider.createValue(object.intValue());
        } else if (object instanceof Long) {
          jsonProviderValue = jsonProvider.createValue(object.longValue());
        } else if (object instanceof Float) {
          jsonProviderValue = jsonProvider.createValue(object.floatValue());
        } else if (object instanceof Double) {
          jsonProviderValue = jsonProvider.createValue(object.doubleValue());
        } else if (object instanceof BigInteger) {
          jsonProviderValue = jsonProvider.createValue((BigInteger) object);
        } else if (object instanceof BigDecimal) {
          jsonProviderValue = jsonProvider.createValue((BigDecimal) object);
        } else {
          throw new IllegalArgumentException("Type converter not found, value " + object);
        }
        return jsonProviderValue;
      },
      Number.class
  );

  public static final Converter<Boolean> BOOLEAN_CONVERTER = new ConverterBase<>(
      (object, myGson) ->
          object == null
              ? JsonValue.NULL
              : object
                  ? JsonValue.TRUE
                  : JsonValue.FALSE
      ,
      Boolean.class
  );

  public static final Converter<Collection> COLLECTION_CONVERTER = new ConverterBase<>(
      (object, myGson) -> {
        JsonArrayBuilder arrayBuilder = myGson.getJsonProvider().createArrayBuilder();
        object.forEach(el -> {
          arrayBuilder.add(myGson.toJsonValue(el));
        });
        return arrayBuilder.build();
      },
      Collection.class
  );


  public static final Converter<Object> ARRAY_CONVERTER = new ConverterBase<>(
      Class::isArray,
      (object, myGson) -> {
        JsonArrayBuilder arrayBuilder = myGson.getJsonProvider().createArrayBuilder();
        for (int index = 0; index < Array.getLength(object); index++) {
          arrayBuilder.add(myGson.toJsonValue(Array.get(object, index)));
        }
        return arrayBuilder.build();
      },
      Object.class
  );


  public static final Converter<Object> SUPER_OBJECT_CLASS_CONVERTER = new ConverterBase<>(
      (Class<?> clazz) -> clazz.equals(Object.class),
      (object, myGson) -> myGson.getJsonProvider().createObjectBuilder().build(),
      Object.class
  );

  public static final Converter<Map> MAP_CONVERTER = new ConverterBase<>(
      Converters::adaptMap,
      Map.class
  );

  private static JsonObject adaptMap(Map<String, Object> map, MyGson myGson) {
    var builder = myGson.getJsonProvider().createObjectBuilder();
    map.forEach((name, value) -> builder.add(name, myGson.toJsonValue(value)));
    return builder.build();
  }

  public static final Converter<Object> OBJECT_CLASS_CONVERTER = new ConverterBase<>(
      (Class<?> clazz) -> !Object.class.equals(clazz),
      Converters::objectAdapter,
      Object.class
  );

  private static Collection<Field> getAllFields(Class clazz) {
    var list = new ArrayList<Field>();
    list.addAll(Arrays.asList(clazz.getDeclaredFields()));
    if (!clazz.getSuperclass().equals(Object.class)) {
      list.addAll(getAllFields(clazz.getSuperclass()));
    }
    return list;
  }

  @SneakyThrows
  private static JsonValue objectAdapter(Object object, MyGson myGson) {
    var map = new LinkedHashMap<String, Object>();
    for (Field field : getAllFields(object.getClass())) {
      if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
        continue;
      }
      field.setAccessible(true);
      var fieldValue = field.get(object);
      map.put(field.getName(), fieldValue);
    }
    return MAP_CONVERTER.toJson(map, myGson);
  }
}
