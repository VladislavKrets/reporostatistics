package online.omnia.statistics;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 27.12.2017.
 */
public class JsonCampaignGroupListDeserializer implements JsonDeserializer<List<Integer>>{
    @Override
    public List<Integer> deserialize(JsonElement jsonElement, Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonArray array = object.get("campaign_groups").getAsJsonArray();
        List<Integer> campaignGroups = new ArrayList<>();
        for (JsonElement element : array) {
            campaignGroups.add(element.getAsJsonObject().get("campaign_group").getAsInt());
        }
        return campaignGroups;
    }
}
