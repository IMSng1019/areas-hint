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
                            
                            // 遍历JsonObject的键值对，提取坐标
                            for (Map.Entry<String, JsonElement> entry : vertexObject.entrySet()) {
                                // 忽略键，直接读取值（假设格式为{x,z}）
                                if (entry.getValue().isJsonPrimitive()) {
                                    // 第一个值为x，第二个值为z
                                    if (x == 0) {
                                        x = entry.getValue().getAsDouble();
                                    } else {
                                        z = entry.getValue().getAsDouble();
                                        break;
                                    }
                                }
                            }
                            vertices.add(new AreaData.Vertex(x, z));
                        } else if (element.isJsonArray()) {
                            // 处理数组格式的顶点 [x, z]
                            JsonArray vertexArray = element.getAsJsonArray();
                            if (vertexArray.size() >= 2) {
                                double x = vertexArray.get(0).getAsDouble();
                                double z = vertexArray.get(1).getAsDouble();
                                vertices.add(new AreaData.Vertex(x, z));
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
                            
                            // 遍历JsonObject的键值对，提取坐标
                            for (Map.Entry<String, JsonElement> entry : vertexObject.entrySet()) {
                                // 忽略键，直接读取值（假设格式为{x,z}）
                                if (entry.getValue().isJsonPrimitive()) {
                                    // 第一个值为x，第二个值为z
                                    if (x == 0) {
                                        x = entry.getValue().getAsDouble();
                                    } else {
                                        z = entry.getValue().getAsDouble();
                                        break;
                                    }
                                }
                            }
                            secondVertices.add(new AreaData.Vertex(x, z));
                        } else if (element.isJsonArray()) {
                            // 处理数组格式的顶点 [x, z]
                            JsonArray vertexArray = element.getAsJsonArray();
                            if (vertexArray.size() >= 2) {
                                double x = vertexArray.get(0).getAsDouble();
                                double z = vertexArray.get(1).getAsDouble();
                                secondVertices.add(new AreaData.Vertex(x, z));
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
                JsonArray array = json.getAsJsonArray();
                if (array.size() >= 2) {
                    double x = array.get(0).getAsDouble();
                    double z = array.get(1).getAsDouble();
                    return new AreaData.Vertex(x, z);
                }
            } else if (json.isJsonObject()) {
                JsonObject object = json.getAsJsonObject();
                // 遍历对象的键值对，查找坐标
                double x = 0;
                double z = 0;
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    if (entry.getValue().isJsonPrimitive()) {
                        if (x == 0) {
                            x = entry.getValue().getAsDouble();
                        } else {
                            z = entry.getValue().getAsDouble();
                            break;
                        }
                    }
                }
                return new AreaData.Vertex(x, z);
            }
            
            throw new JsonParseException("无法解析顶点数据");
        }
    }
} 