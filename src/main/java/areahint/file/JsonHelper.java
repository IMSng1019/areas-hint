package areahint.file;

import areahint.Areashint;
import areahint.data.AreaData;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON处理工具类
 * 用于解析和转换JSON数据
 */
public class JsonHelper {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(AreaData.class, new AreaDataDeserializer())
            .registerTypeAdapter(AreaData.Vertex.class, new VertexDeserializer())
            .setPrettyPrinting()
            .create();
    
    /**
     * 将JSON字符串转换为区域数据
     * @param json JSON字符串
     * @return 区域数据，如果解析失败则返回null
     */
    public static AreaData fromJson(String json) {
        try {
            return GSON.fromJson(json, AreaData.class);
        } catch (JsonSyntaxException e) {
            Areashint.LOGGER.error("JSON解析错误: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 将区域数据转换为JSON字符串
     * @param areaData 区域数据
     * @return JSON字符串
     */
    public static String toJson(AreaData areaData) {
        return GSON.toJson(areaData);
    }
    
    /**
     * 将区域数据列表转换为JSON字符串
     * @param areaDataList 区域数据列表
     * @return JSON字符串
     */
    public static String toJson(List<AreaData> areaDataList) {
        return GSON.toJson(areaDataList);
    }
    
    /**
     * 区域数据反序列化器
     * 用于将JSON对象转换为区域数据对象
     */
    private static class AreaDataDeserializer implements JsonDeserializer<AreaData> {
        @Override
        public AreaData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            
            String name = null;
            List<AreaData.Vertex> vertices = new ArrayList<>();
            List<AreaData.Vertex> secondVertices = new ArrayList<>();
            int level = 0;
            String baseName = null;
            
            try {
                // 解析名称
                if (jsonObject.has("name")) {
                    name = jsonObject.get("name").getAsString();
                }
                
                // 解析一级顶点
                if (jsonObject.has("vertices") && jsonObject.get("vertices").isJsonArray()) {
                    JsonArray verticesArray = jsonObject.getAsJsonArray("vertices");
                    for (JsonElement element : verticesArray) {
                        if (element.isJsonObject()) {
                            JsonObject vertexObject = element.getAsJsonObject();
                            double x = 0;
                            double z = 0;
                            
                            // 标准格式: {"x": 值, "z": 值}
                            if (vertexObject.has("x") && vertexObject.has("z")) {
                                x = vertexObject.get("x").getAsDouble();
                                z = vertexObject.get("z").getAsDouble();
                                vertices.add(new AreaData.Vertex(x, z));
                            } else {
                                Areashint.LOGGER.warn("顶点缺少x或z坐标: " + vertexObject);
                            }
                        } else if (element.isJsonArray()) {
                            // 数组格式 [x, z] - 转换为标准格式
                            JsonArray vertexArray = element.getAsJsonArray();
                            if (vertexArray.size() >= 2) {
                                double x = vertexArray.get(0).getAsDouble();
                                double z = vertexArray.get(1).getAsDouble();
                                vertices.add(new AreaData.Vertex(x, z));
                                Areashint.LOGGER.info("转换数组格式顶点 [" + x + "," + z + "] 为标准格式");
                            }
                        }
                    }
                }
                
                // 解析二级顶点
                if (jsonObject.has("second-vertices") && jsonObject.get("second-vertices").isJsonArray()) {
                    JsonArray secondVerticesArray = jsonObject.getAsJsonArray("second-vertices");
                    for (JsonElement element : secondVerticesArray) {
                        if (element.isJsonObject()) {
                            JsonObject vertexObject = element.getAsJsonObject();
                            double x = 0;
                            double z = 0;
                            
                            // 标准格式: {"x": 值, "z": 值}
                            if (vertexObject.has("x") && vertexObject.has("z")) {
                                x = vertexObject.get("x").getAsDouble();
                                z = vertexObject.get("z").getAsDouble();
                                secondVertices.add(new AreaData.Vertex(x, z));
                            } else {
                                Areashint.LOGGER.warn("二级顶点缺少x或z坐标: " + vertexObject);
                            }
                        } else if (element.isJsonArray()) {
                            // 数组格式 [x, z] - 转换为标准格式
                            JsonArray vertexArray = element.getAsJsonArray();
                            if (vertexArray.size() >= 2) {
                                double x = vertexArray.get(0).getAsDouble();
                                double z = vertexArray.get(1).getAsDouble();
                                secondVertices.add(new AreaData.Vertex(x, z));
                                Areashint.LOGGER.info("转换数组格式二级顶点 [" + x + "," + z + "] 为标准格式");
                            }
                        }
                    }
                }
                
                // 解析等级
                if (jsonObject.has("level")) {
                    level = jsonObject.get("level").getAsInt();
                }
                
                // 解析上级域名
                if (jsonObject.has("base-name") && !jsonObject.get("base-name").isJsonNull()) {
                    baseName = jsonObject.get("base-name").getAsString();
                }
                
            } catch (Exception e) {
                throw new JsonParseException("解析区域数据时出错: " + e.getMessage());
            }
            
            return new AreaData(name, vertices, secondVertices, level, baseName);
        }
    }
    
    /**
     * 顶点反序列化器
     * 用于将JSON对象转换为顶点对象
     */
    private static class VertexDeserializer implements JsonDeserializer<AreaData.Vertex> {
        @Override
        public AreaData.Vertex deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonArray()) {
                // 处理数组格式 [x, z]
                JsonArray array = json.getAsJsonArray();
                if (array.size() >= 2) {
                    double x = array.get(0).getAsDouble();
                    double z = array.get(1).getAsDouble();
                    Areashint.LOGGER.info("从数组格式转换顶点: [" + x + "," + z + "]");
                    return new AreaData.Vertex(x, z);
                }
            } else if (json.isJsonObject()) {
                // 处理对象格式 {"x": 值, "z": 值}
                JsonObject object = json.getAsJsonObject();
                if (object.has("x") && object.has("z")) {
                    double x = object.get("x").getAsDouble();
                    double z = object.get("z").getAsDouble();
                    return new AreaData.Vertex(x, z);
                }
            }
            
            throw new JsonParseException("无法解析顶点数据，请使用标准格式 {\"x\": 值, \"z\": 值}");
        }
    }
} 