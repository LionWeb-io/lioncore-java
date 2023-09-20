package io.lionweb.lioncore.java.serialization;

import static io.lionweb.lioncore.java.serialization.SerializedJsonComparisonUtils.assertEquivalentLionWebJson;
import static org.junit.Assert.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.Language;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.Node;
import io.lionweb.lioncore.java.model.impl.DynamicNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/** Specific tests of JsonSerialization using the LionCore example. */
public class SerializationOfLionCoreTest extends SerializationTest {

  @Test
  public void serializeLionCoreToSerializedChunk() {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    SerializedChunk serializedChunk =
        jsonSerialization.serializeTreeToSerializationBlock(LionCore.getInstance());

    assertEquals("1", serializedChunk.getSerializationFormatVersion());

    assertEquals(1, serializedChunk.getLanguages().size());
    Assert.assertEquals(
        new UsedLanguage("LionCore-M3", "1"), serializedChunk.getLanguages().get(0));

    SerializedClassifierInstance LionCore_M3 =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> n.getID().equals("-id-LionCore-M3"))
            .findFirst()
            .get();
    assertEquals("-id-LionCore-M3", LionCore_M3.getID());
    assertEquals(new MetaPointer("LionCore-M3", "1", "Language"), LionCore_M3.getClassifier());
    assertEquals(
        Arrays.asList(
            new SerializedPropertyValue(
                new MetaPointer("LionCore-M3", "1", "Language-version"), "1"),
            new SerializedPropertyValue(
                new MetaPointer("LionCore-M3", "1", "IKeyed-key"), "LionCore-M3"),
            new SerializedPropertyValue(
                new MetaPointer("LionCore-builtins", "1", "LionCore-builtins-INamed-name"),
                "LionCore.M3")),
        LionCore_M3.getProperties());
    assertEquals(
        Arrays.asList(
            new SerializedContainmentValue(
                new MetaPointer("LionCore-M3", "1", "Language-entities"),
                Arrays.asList(
                    "-id-Annotation",
                    "-id-Concept",
                    "-id-ConceptInterface",
                    "-id-Containment",
                    "-id-DataType",
                    "-id-Enumeration",
                    "-id-EnumerationLiteral",
                    "-id-Feature",
                    "-id-Classifier",
                    "-id-Link",
                    "-id-Language",
                    "-id-LanguageEntity",
                    "-id-IKeyed",
                    "-id-PrimitiveType",
                    "-id-Property",
                    "-id-Reference"))),
        LionCore_M3.getContainments());
    assertEquals(
        Arrays.asList(
            new SerializedReferenceValue(
                new MetaPointer("LionCore-M3", "1", "Language-dependsOn"),
                Collections.emptyList())),
        LionCore_M3.getReferences());

    SerializedClassifierInstance LionCore_M3_ConceptInterface_extends =
        serializedChunk.getClassifierInstances().stream()
            .filter(n -> n.getID().equals("-id-ConceptInterface-extends"))
            .findFirst()
            .get();
  }

  @Test
  public void serializeLionCoreToJSON() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement serializedElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    JsonElement reserialized = jsonSerialization.serializeTreeToJsonElement(LionCore.getInstance());
    assertEquivalentLionWebJson(
        serializedElement.getAsJsonObject(), reserialized.getAsJsonObject());
  }

  @Test
  public void unserializeLionCoreToSerializedChunk() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    SerializedChunk serializedChunk =
        new LowLevelJsonSerialization().unserializeSerializationBlock(jsonElement);
    List<SerializedClassifierInstance> unserializedSerializedClassifierInstanceData =
        serializedChunk.getClassifierInstances();

    SerializedNodeInstance lioncore =
        (SerializedNodeInstance) serializedChunk.getInstanceByID("-id-LionCore-M3");
    assertEquals(MetaPointer.from(LionCore.getLanguage()), lioncore.getClassifier());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore.M3", lioncore.getPropertyValue("LionCore-builtins-INamed-name"));
    assertEquals(16, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParentNodeID());
  }

  @Test
  public void unserializeLionCoreToConcreteClasses() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    Language lioncore = (Language) unserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getConcept());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore.M3", lioncore.getName());
    assertEquals(16, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParent());
  }

  @Test
  public void unserializeLionCoreToDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
    jsonSerialization.getInstanceResolver().addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization
        .getInstanceResolver()
        .addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
    jsonSerialization.getClassifierResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization.getInstantiator().enableDynamicNodes();
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    List<Node> unserializedNodes = jsonSerialization.unserializeToNodes(jsonElement);

    DynamicNode lioncore = (DynamicNode) unserializedNodes.get(0);
    assertEquals(LionCore.getLanguage(), lioncore.getConcept());
    assertEquals("-id-LionCore-M3", lioncore.getID());
    assertEquals("LionCore.M3", lioncore.getPropertyValueByName("name"));
    assertEquals(16, lioncore.getChildren().size());
    assertEquals(null, lioncore.getParent());
  }

  @Test(expected = RuntimeException.class)
  public void unserializeLionCoreFailsWithoutRegisteringTheClassesOrEnablingDynamicNodes() {
    InputStream inputStream = this.getClass().getResourceAsStream("/serialization/lioncore.json");
    JsonElement jsonElement = JsonParser.parseReader(new InputStreamReader(inputStream));
    JsonSerialization jsonSerialization = JsonSerialization.getBasicSerialization();
    jsonSerialization.getClassifierResolver().registerLanguage(LionCore.getInstance());
    jsonSerialization
        .getPrimitiveValuesSerialization()
        .registerLionBuiltinsPrimitiveSerializersAndUnserializers();
    jsonSerialization.unserializeToNodes(jsonElement);
  }
}
