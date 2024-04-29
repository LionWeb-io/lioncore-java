package io.lionweb.lioncore.java.serialization;

import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import io.lionweb.lioncore.java.api.ClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.CompositeClassifierInstanceResolver;
import io.lionweb.lioncore.java.api.LocalClassifierInstanceResolver;
import io.lionweb.lioncore.java.language.*;
import io.lionweb.lioncore.java.model.*;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.utils.NetworkUtils;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is responsible for deserializing models.
 *
 * <p>The deserialization of each node _requires_ the deserializer to be able to resolve the Concept
 * used. If this requirement is not satisfied the deserialization will fail. The actual class
 * implementing Node being instantiated will depend on the configuration. Specific classes for
 * specific Concepts can be registered, and the usage of DynamicNode for all others can be enabled.
 *
 * <p>Note that by default JsonSerialization will require specific Node subclasses to be specified.
 * For example, it will need to know that the concept with id 'foo-library' can be deserialized to
 * instances of the class Library. If you want serialization to instantiate DynamicNodes for
 * concepts for which you do not have a corresponding Node subclass, then you need to enable that
 * behavior explicitly by calling getNodeInstantiator().enableDynamicNodes().
 */
public class JsonSerialization {
  public static final String DEFAULT_SERIALIZATION_FORMAT = "2023.1";

  public static void saveLanguageToFile(Language language, File file) throws IOException {
    file.getParentFile().mkdirs();
    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
      getStandardSerialization().serializeTreesToJson(os, language);
    }
  }

  /**
   * Load a single Language from a file. If the file contains more than one language an exception is
   * thrown.
   */
  public Language loadLanguage(File file) throws IOException {
    Language language;
    try (FileInputStream fileInputStream = new FileInputStream(file)) {
      language = loadLanguage(fileInputStream);
    }
    return language;
  }

  /**
   * Load a single Language from an InputStream. If the InputStream contains more than one language
   * an exception is thrown.
   */
  public Language loadLanguage(InputStream inputStream) {
    JsonSerialization jsonSerialization = JsonSerialization.getStandardSerialization();
    List<Node> lNodes = jsonSerialization.deserializeToNodes(inputStream);
    List<Language> languages =
        lNodes.stream()
            .filter(n -> n instanceof Language)
            .map(n -> (Language) n)
            .collect(Collectors.toList());
    if (languages.size() != 1) {
      throw new IllegalStateException();
    }
    return languages.get(0);
  }

  /** This has specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getStandardSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    jsonSerialization.classifierResolver.registerLanguage(LionCore.getInstance());
    jsonSerialization.instantiator.registerLionCoreCustomDeserializers();
    jsonSerialization.primitiveValuesSerialization
        .registerLionBuiltinsPrimitiveSerializersAndDeserializers();
    jsonSerialization.instanceResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
    jsonSerialization.instanceResolver.addAll(
        LionCoreBuiltins.getInstance().thisAndAllDescendants());
    return jsonSerialization;
  }

  /** This has no specific support for LionCore or LionCoreBuiltins. */
  public static JsonSerialization getBasicSerialization() {
    JsonSerialization jsonSerialization = new JsonSerialization();
    return jsonSerialization;
  }

  private final ClassifierResolver classifierResolver;
  private final Instantiator instantiator;
  private final PrimitiveValuesSerialization primitiveValuesSerialization;

  private final LocalClassifierInstanceResolver instanceResolver;

  /**
   * This guides what we do when deserializing a sub-tree and not being able to resolve the parent.
   */
  private UnavailableNodePolicy unavailableParentPolicy = UnavailableNodePolicy.THROW_ERROR;

  /**
   * This guides what we do when deserializing a sub-tree and not being able to resolve the
   * children.
   */
  private UnavailableNodePolicy unavailableChildrenPolicy = UnavailableNodePolicy.THROW_ERROR;

  /**
   * This guides what we do when deserializing a sub-tree and not being able to resolve a reference
   * target.
   */
  private UnavailableNodePolicy unavailableReferenceTargetPolicy =
      UnavailableNodePolicy.THROW_ERROR;

  private JsonSerialization() {
    // prevent public access
    classifierResolver = new ClassifierResolver();
    instantiator = new Instantiator();
    primitiveValuesSerialization = new PrimitiveValuesSerialization();
    instanceResolver = new LocalClassifierInstanceResolver();
  }

  //
  // Configuration
  //

  public ClassifierResolver getClassifierResolver() {
    return classifierResolver;
  }

  public Instantiator getInstantiator() {
    return instantiator;
  }

  public PrimitiveValuesSerialization getPrimitiveValuesSerialization() {
    return primitiveValuesSerialization;
  }

  public LocalClassifierInstanceResolver getInstanceResolver() {
    return instanceResolver;
  }

  public void enableDynamicNodes() {
    instantiator.enableDynamicNodes();
    primitiveValuesSerialization.enableDynamicNodes();
  }

  public @Nonnull UnavailableNodePolicy getUnavailableParentPolicy() {
    return this.unavailableParentPolicy;
  }

  public @Nonnull UnavailableNodePolicy getUnavailableReferenceTargetPolicy() {
    return this.unavailableReferenceTargetPolicy;
  }

  public @Nonnull UnavailableNodePolicy getUnavailableChildrenPolicy() {
    return this.unavailableChildrenPolicy;
  }

  public void setUnavailableParentPolicy(@Nonnull UnavailableNodePolicy unavailableParentPolicy) {
    Objects.requireNonNull(unavailableParentPolicy);
    this.unavailableParentPolicy = unavailableParentPolicy;
  }

  public void setUnavailableChildrenPolicy(
      @Nonnull UnavailableNodePolicy unavailableChildrenPolicy) {
    Objects.requireNonNull(unavailableChildrenPolicy);
    this.unavailableChildrenPolicy = unavailableChildrenPolicy;
  }

  public void setUnavailableReferenceTargetPolicy(
      @Nonnull UnavailableNodePolicy unavailableReferenceTargetPolicy) {
    Objects.requireNonNull(unavailableReferenceTargetPolicy);
    this.unavailableReferenceTargetPolicy = unavailableReferenceTargetPolicy;
  }

  //
  // Serialization
  //

  public SerializedChunk serializeTreeToSerializationBlock(Node root) {
    return serializeNodesToSerializationBlock(root.thisAndAllDescendants());
  }

  public SerializedChunk serializeNodesToSerializationBlock(List<Node> nodes) {
    return serializeNodesToSerializationBlock(nodes.stream());
  }

  public SerializedChunk serializeNodesToSerializationBlock(Stream<Node> nodes) {
    SerializedChunk serializedChunk = new SerializedChunk();
    serializedChunk.setSerializationFormatVersion(DEFAULT_SERIALIZATION_FORMAT);
    nodes.forEach(node -> {
      try {
        if (Integer.parseInt(node.getID()) % 10_000 == 0) {
          System.out.println(node.getID());
        }} catch (NumberFormatException e) {
      }
      Objects.requireNonNull(node, "nodes should not contain null values");
      serializedChunk.addClassifierInstance(serializeNode(node));
      node.getAnnotations()
              .forEach(
                      annotationInstance -> {
                        serializedChunk.addClassifierInstance(
                                serializeAnnotationInstance(annotationInstance));
                      });
      Objects.requireNonNull(
              node.getConcept(), "A node should have a concept in order to be serialized");
      Objects.requireNonNull(
              node.getConcept().getLanguage(),
              "A Concept should be part of a Language in order to be serialized. Concept "
                      + node.getConcept()
                      + " is not");
      considerLanguageDuringSerialization(serializedChunk, node.getConcept().getLanguage());
      node.getConcept()
              .allFeatures()
              .forEach(
                      f -> considerLanguageDuringSerialization(serializedChunk, f.getDeclaringLanguage()));
      node.getConcept()
              .allProperties()
              .forEach(
                      p -> considerLanguageDuringSerialization(serializedChunk, p.getType().getLanguage()));
      node.getConcept()
              .allLinks()
              .forEach(
                      l -> considerLanguageDuringSerialization(serializedChunk, l.getType().getLanguage()));
    });
    return serializedChunk;
  }

  private void considerLanguageDuringSerialization(
      SerializedChunk serializedChunk, Language language) {
    registerLanguage(language);
    UsedLanguage languageKeyVersion = UsedLanguage.fromLanguage(language);
    if (!serializedChunk.getLanguages().contains(languageKeyVersion)) {
      serializedChunk.addLanguage(languageKeyVersion);
    }
  }

  public SerializedChunk serializeNodesToSerializationBlock(Node... nodes) {
    return serializeNodesToSerializationBlock(Arrays.asList(nodes));
  }

  public JsonElement serializeTreeToJsonElement(Node node) {
    if (node instanceof ProxyNode) {
      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
    }
    return serializeNodesToJsonElement(
        node.thisAndAllDescendants().stream()
            .filter(n -> !(n instanceof ProxyNode))
            .collect(Collectors.toList()));
  }

  public JsonElement serializeTreesToJsonElement(Node... roots) {
    Set<String> nodesIDs = new HashSet<>();
    Stream<Node> allNodes = Arrays.stream(roots)
            .flatMap(this::thisAndAllDescendants)
            .filter(n -> !(n instanceof ProxyNode))
            .filter(n -> {
                // We support serialization of incorrect nodes, so we allow nodes without ID to be
                // serialized
                if (n.getID() != null) {
                  if (!nodesIDs.contains(n.getID())) {
                                        nodesIDs.add(n.getID());
                                        return true;
                  }
                } else {
                  return true;
                }
                return false;
              });

    return serializeNodesToJsonElement(        allNodes);
  }

  private Stream<Node> thisAndAllDescendants(Node root) {
    return Stream.concat(Stream.of(root), root.getChildren().stream());
  }

  public JsonElement serializeNodesToJsonElement(List<Node> nodes) {
    if (nodes.stream().anyMatch(n -> n instanceof ProxyNode)) {
      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
    }
    SerializedChunk serializationBlock = serializeNodesToSerializationBlock(nodes);
    return new LowLevelJsonSerialization().serializeToJsonElement(serializationBlock);
  }

  public JsonElement serializeNodesToJsonElement(Stream<Node> nodes) {
    nodes = nodes.filter(n ->  {
                  if(n instanceof ProxyNode) {
                      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
                  }
                  return true;
    });
    SerializedChunk serializationBlock = serializeNodesToSerializationBlock(nodes);
    return new LowLevelJsonSerialization().serializeToJsonElement(serializationBlock);
  }

  public JsonElement serializeNodesToJsonElement(Node... nodes) {
    return serializeNodesToJsonElement(Arrays.asList(nodes));
  }

  /**
   * @deprecated Use {@link #serializeTreeToJson(OutputStream, Node)}
   */
  @Deprecated()
  public String serializeTreeToJsonString(Node node) {
    return jsonElementToString(serializeTreeToJsonElement(node));
  }

  /**
   * @deprecated Use {@link #serializeTreesToJson(OutputStream, Node...)}
   */
  @Deprecated()
  public String serializeTreesToJsonString(Node... nodes) {
    return jsonElementToString(serializeTreesToJsonElement(nodes));
  }

  /**
   * @deprecated Use {@link #serializeNodesToJson(OutputStream, List<Node>)}
   */
  @Deprecated()
  public String serializeNodesToJsonString(List<Node> nodes) {
    return jsonElementToString(serializeNodesToJsonElement(nodes));
  }

  /**
   * @deprecated Use {@link #serializeNodesToJson(OutputStream, Node...)}
   */
  @Deprecated()
  public String serializeNodesToJsonString(Node... nodes) {
    return jsonElementToString(serializeNodesToJsonElement(nodes));
  }

  public void serializeTreeToJson(OutputStream outputStream, Node node) {
    serializeJsonElement(outputStream, serializeTreeToJsonElement(node));
  }

    public void serializeTreesToJson(OutputStream outputStream, Node... nodes) {
        Set<String> nodesIDs = new HashSet<>();
        Supplier<Stream<Node>> allNodes = () -> Arrays.stream(nodes)
                .flatMap(this::thisAndAllDescendants)
                .filter(n -> !(n instanceof ProxyNode))
                .filter(n -> {
                    // We support serialization of incorrect nodes, so we allow nodes without ID to be
                    // serialized
                    try {
                        if (Integer.parseInt(n.getID()) % 10_000 == 0) {
                            System.out.println(n.getID());
                        }
                    } catch (NumberFormatException e) {
                    }
                    if (n.getID() != null) {
                        if (!nodesIDs.contains(n.getID())) {
                            nodesIDs.add(n.getID());
                            return true;
                        }
                    } else {
                        return true;
                    }
                    return false;
                });
        Stream<Language> languages = allNodes.get().flatMap(n ->
                        Stream.concat(Stream.of(n.getClassifier().getLanguage()), n.getConcept()
                                .allFeatures()
                                .stream()
                                .flatMap(f -> Stream.of(
                                        f.getDeclaringLanguage(),
                                        f instanceof Property ?
                                                ((Property) f).getType().getLanguage() :
                                                ((Link) f).getType().getLanguage())
                                )))
                .distinct();

        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(outputStream))) {
            writer.setSerializeNulls(true);
            writer.setIndent("  ");
            writer.beginObject();
            {
                writer.name("serializationFormatVersion").value(DEFAULT_SERIALIZATION_FORMAT);
                writer.name("languages").beginArray();
                for (Language l : (Iterable<Language>) languages::iterator) {
                    writer.beginObject();
                    writer.name("key").value(l.getKey());
                    writer.name("version").value(l.getVersion());
                    writer.endObject();
                }

                nodesIDs.clear();

                writer.endArray();
                writer.name("nodes").beginArray();
                writer.beginObject();
                for (Node n : (Iterable<Node>) allNodes.get()::iterator) {
                    writer.name("id").value(n.getID());
                    writer.name("classifier");
                    serializeToJsonElement(writer, MetaPointer.from(n.getClassifier()));
                    writer.name("properties").beginArray();
                    for (Property p : n.getClassifier().allProperties()) {
                        writer.beginObject();
                        {
                            writer.name("property");
                            serializeToJsonElement(writer, MetaPointer.from(p));
                            writer.name("value").value(serializePropertyValue(p.getType(), n.getPropertyValue(p)));
                        }
                        writer.endObject();
                    }
                    writer.endArray();
                    writer.name("containments").beginArray();
                    for (Containment c : n.getClassifier().allContainments()) {
                        writer.beginObject();
                        {
                            writer.name("containment");
                            serializeToJsonElement(writer, MetaPointer.from(c));
                            writer.name("children").beginArray();
                            for (Node ch : n.getChildren(c)) {
                                writer.value(ch.getID());
                            }
                            writer.endArray();
                        }
                        writer.endObject();
                    }
                    writer.endArray();
                    writer.name("references").beginArray();
                    for (Reference r : n.getClassifier().allReferences()) {
                        writer.beginObject();
                        {
                            writer.name("reference");
                            serializeToJsonElement(writer, MetaPointer.from(r));
                            writer.name("targets").beginArray();
                            for (ReferenceValue rv : n.getReferenceValues(r)) {
                                writer.beginObject();
                                writer.name("resolveInfo").value(rv.getResolveInfo());
                                writer.name("reference").value(rv.getReferredID());
                                writer.endObject();
                            }
                            writer.endArray();
                        }
                        writer.endObject();
                    }
                    writer.endArray();
                    writer.name("annotations").beginArray();
                    for (AnnotationInstance a : n.getAnnotations()) {
                        writer.value(a.getID());
                    }
                    writer.endArray();
                    writer.name("parent").value(n.getParent() != null ? n.getParent().getID() : null);
                }
                writer.endObject();
                writer.endArray();
            }
            writer.endObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void serializeToJsonElement(JsonWriter writer, MetaPointer metapointer) throws IOException {
      writer.beginObject();
        {
            writer.name("language").value(metapointer.getLanguage());
            writer.name("version").value(metapointer.getVersion());
            writer.name("key").value(metapointer.getKey());

        }
        writer.endObject();
    }


    public void serializeNodesToJson(OutputStream outputStream, List<Node> nodes) {
    serializeJsonElement(outputStream, serializeNodesToJsonElement(nodes));
  }

  public void serializeNodesToJson(OutputStream outputStream, Node... nodes) {
    serializeJsonElement(outputStream, serializeNodesToJsonElement(nodes));
  }

  //
  // Serialization - Private
  //

  /**
   * @deprecated Use {@link #serializeJsonElement(OutputStream, JsonElement)}
   */
  @Deprecated
  private String jsonElementToString(JsonElement element) {
    return new GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(element);
  }

  private void serializeJsonElement(OutputStream outputStream, JsonElement element) {
    new GsonBuilder().serializeNulls().create().toJson(element, new OutputStreamWriter(outputStream));
  }

  private SerializedClassifierInstance serializeNode(@Nonnull Node node) {
    Objects.requireNonNull(node, "Node should not be null");
    SerializedClassifierInstance serializedClassifierInstance = new SerializedClassifierInstance();
    serializedClassifierInstance.setID(node.getID());
    serializedClassifierInstance.setClassifier(MetaPointer.from(node.getConcept()));
    if (node.getParent() != null) {
      serializedClassifierInstance.setParentNodeID(node.getParent().getID());
    }
    serializeProperties(node, serializedClassifierInstance);
    serializeContainments(node, serializedClassifierInstance);
    serializeReferences(node, serializedClassifierInstance);
    serializeAnnotations(node, serializedClassifierInstance);
    return serializedClassifierInstance;
  }

  private SerializedClassifierInstance serializeAnnotationInstance(
      @Nonnull AnnotationInstance annotationInstance) {
    Objects.requireNonNull(annotationInstance, "AnnotationInstance should not be null");
    SerializedClassifierInstance serializedClassifierInstance = new SerializedClassifierInstance();
    serializedClassifierInstance.setID(annotationInstance.getID());
    serializedClassifierInstance.setParentNodeID(annotationInstance.getParent().getID());
    serializedClassifierInstance.setClassifier(
        MetaPointer.from(annotationInstance.getAnnotationDefinition()));
    serializeProperties(annotationInstance, serializedClassifierInstance);
    serializeContainments(annotationInstance, serializedClassifierInstance);
    serializeReferences(annotationInstance, serializedClassifierInstance);
    serializeAnnotations(annotationInstance, serializedClassifierInstance);
    return serializedClassifierInstance;
  }

  private static void serializeAnnotations(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    serializedClassifierInstance.setAnnotations(
        classifierInstance.getAnnotations().stream()
            .map(a -> a.getID())
            .collect(Collectors.toList()));
  }

  private static void serializeReferences(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    classifierInstance
        .getClassifier()
        .allReferences()
        .forEach(
            reference -> {
              SerializedReferenceValue referenceValue = new SerializedReferenceValue();
              referenceValue.setMetaPointer(
                  MetaPointer.from(
                      reference, ((LanguageEntity) reference.getContainer()).getLanguage()));
              referenceValue.setValue(
                  classifierInstance.getReferenceValues(reference).stream()
                      .map(
                          rv -> {
                            String referredID =
                                rv.getReferred() == null ? null : rv.getReferred().getID();
                            return new SerializedReferenceValue.Entry(
                                referredID, rv.getResolveInfo());
                          })
                      .collect(Collectors.toList()));
              serializedClassifierInstance.addReferenceValue(referenceValue);
            });
  }

  private static void serializeContainments(
      @Nonnull ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    Objects.requireNonNull(classifierInstance, "ClassifierInstance should not be null");
    classifierInstance
        .getClassifier()
        .allContainments()
        .forEach(
            containment -> {
              SerializedContainmentValue containmentValue = new SerializedContainmentValue();
              containmentValue.setMetaPointer(
                  MetaPointer.from(
                      containment, ((LanguageEntity) containment.getContainer()).getLanguage()));
              containmentValue.setValue(
                  classifierInstance.getChildren(containment).stream()
                      .map(c -> c.getID())
                      .collect(Collectors.toList()));
              serializedClassifierInstance.addContainmentValue(containmentValue);
            });
  }

  private void serializeProperties(
      ClassifierInstance<?> classifierInstance,
      SerializedClassifierInstance serializedClassifierInstance) {
    classifierInstance
        .getClassifier()
        .allProperties()
        .forEach(
            property -> {
              SerializedPropertyValue propertyValue = new SerializedPropertyValue();
              propertyValue.setMetaPointer(
                  MetaPointer.from(
                      property, ((LanguageEntity) property.getContainer()).getLanguage()));
              propertyValue.setValue(
                  serializePropertyValue(
                      property.getType(), classifierInstance.getPropertyValue(property)));
              serializedClassifierInstance.addPropertyValue(propertyValue);
            });
  }

  //
  // Deserialization
  //

  public List<Node> deserializeToNodes(File file) throws FileNotFoundException {
    return deserializeToNodes(new FileInputStream(file));
  }

  public List<Node> deserializeToNodes(JsonElement jsonElement) {
    return deserializeToClassifierInstances(jsonElement).stream()
        .filter(ci -> ci instanceof Node)
        .map(ci -> (Node) ci)
        .collect(Collectors.toList());
  }

  public List<ClassifierInstance<?>> deserializeToClassifierInstances(JsonElement jsonElement) {
    SerializedChunk serializationBlock =
        new LowLevelJsonSerialization().deserializeSerializationBlock(jsonElement);
    validateSerializationBlock(serializationBlock);
    return deserializeSerializationBlock(serializationBlock);
  }

  public List<Node> deserializeToNodes(URL url) throws IOException {
    try (InputStream is = NetworkUtils.urlToInputStream(url, null)) {
      return deserializeToNodes(is);
    }
  }

  /**
   * @deprecated Use {{@link #deserializeToNodes(InputStream)}}
   */
  @Deprecated
  public List<Node> deserializeToNodes(String json) {
    return deserializeToNodes(JsonParser.parseString(json));
  }

  public List<Node> deserializeToNodes(InputStream inputStream) {
    return deserializeToNodes(JsonParser.parseReader(new InputStreamReader(inputStream)));
  }

  //
  // Deserialization - Private
  //

  private String serializePropertyValue(@Nonnull DataType dataType, @Nullable Object value) {
    Objects.requireNonNull(dataType == null, "cannot serialize property when the dataType is null");
    Objects.requireNonNull(
        dataType.getID() == null, "cannot serialize property when the dataType.ID is null");
    if (value == null) {
      return null;
    }
    return primitiveValuesSerialization.serialize(dataType.getID(), value);
  }

  private void validateSerializationBlock(SerializedChunk serializationBlock) {
    if (!serializationBlock.getSerializationFormatVersion().equals(DEFAULT_SERIALIZATION_FORMAT)) {
      throw new IllegalArgumentException(
          "Only serializationFormatVersion = '" + DEFAULT_SERIALIZATION_FORMAT + "' is supported");
    }
  }

  /** Create a Proxy and from now on use it to resolve instances for this node ID. */
  ProxyNode createProxy(String nodeID) {
    if (instanceResolver.resolve(nodeID) != null) {
      throw new IllegalStateException(
          "Cannot create a Proxy for node ID "
              + nodeID
              + " has there is already a Classifier Instance available for such ID");
    }
    ProxyNode proxyNode = new ProxyNode(nodeID);
    instanceResolver.add(proxyNode);
    return proxyNode;
  }

  /**
   * This method returned a sorted version of the original list, so that leaves nodes comes first,
   * or in other words that a parent never precedes its children.
   */
  private DeserializationStatus sortLeavesFirst(List<SerializedClassifierInstance> originalList) {
    DeserializationStatus deserializationStatus = new DeserializationStatus(this, originalList);

    // We create the list going from the roots, to their children and so on, and then we will revert
    // the list

    deserializationStatus.putNodesWithNullIDsInFront();

    switch (unavailableParentPolicy) {
      case NULL_REFERENCES:
        {
          // Let's find all the IDs of nodes present here. The nodes with parents not present here
          // are effectively treated as roots and their parent will be set to null, as we cannot
          // retrieve them or set them (until we decide to provide some sort of NodeResolver)
          Set<String> knownIDs =
              originalList.stream().map(ci -> ci.getID()).collect(Collectors.toSet());
          originalList.stream()
              .filter(ci -> !knownIDs.contains(ci.getParentNodeID()))
              .forEach(effectivelyRoot -> deserializationStatus.place(effectivelyRoot));
          break;
        }
      case PROXY_NODES:
        {
          // Let's find all the IDs of nodes present here. The nodes with parents not present here
          // are effectively treated as roots and their parent will be set to an instance of a
          // ProxyNode, as we cannot retrieve them or set them (until we decide to provide some
          // sort of NodeResolver)
          Set<String> knownIDs =
              originalList.stream().map(ci -> ci.getID()).collect(Collectors.toSet());
          Set<String> parentIDs =
              originalList.stream()
                  .map(n -> n.getParentNodeID())
                  .filter(id -> id != null)
                  .collect(Collectors.toSet());
          Set<String> unknownParentIDs = Sets.difference(parentIDs, knownIDs);
          originalList.stream()
              .filter(ci -> unknownParentIDs.contains(ci.getParentNodeID()))
              .forEach(effectivelyRoot -> deserializationStatus.place(effectivelyRoot));

          unknownParentIDs.forEach(id -> deserializationStatus.createProxy(id));
          break;
        }
    }

    // We can start by putting at the start all the elements which either have no parent,
    // or had a parent already added to the list
    while (deserializationStatus.howManySorted() < originalList.size()) {
      int initialLength = deserializationStatus.howManySorted();
      for (int i = 0; i < deserializationStatus.howManyToSort(); i++) {
        SerializedClassifierInstance node = deserializationStatus.getNodeToSort(i);
        if (node.getParentNodeID() == null
            || deserializationStatus
                .streamSorted()
                .anyMatch(sn -> Objects.equals(sn.getID(), node.getParentNodeID()))) {
          deserializationStatus.place(node);
          i--;
        }
      }
      if (initialLength == deserializationStatus.howManySorted()) {
        if (deserializationStatus.howManySorted() == 0) {
          throw new DeserializationException(
              "No root found, we cannot deserialize this tree. Original list: " + originalList);
        } else {
          throw new DeserializationException(
              "Something is not right: we are unable to complete sorting the list "
                  + originalList
                  + ". Probably there is a containment loop");
        }
      }
    }

    deserializationStatus.reverse();
    return deserializationStatus;
  }

  public List<ClassifierInstance<?>> deserializeSerializationBlock(
      SerializedChunk serializationBlock) {
    return deserializeClassifierInstances(serializationBlock.getClassifierInstances());
  }

  private List<ClassifierInstance<?>> deserializeClassifierInstances(
      List<SerializedClassifierInstance> serializedClassifierInstances) {
    // We want to deserialize the nodes starting from the leaves. This is useful because in certain
    // cases we may want to use the children as constructor parameters of the parent
    DeserializationStatus deserializationStatus = sortLeavesFirst(serializedClassifierInstances);
    List<SerializedClassifierInstance> sortedSerializedClassifierInstances =
        deserializationStatus.sortedList;
    if (sortedSerializedClassifierInstances.size() != serializedClassifierInstances.size()) {
      throw new IllegalStateException();
    }
    Map<String, ClassifierInstance<?>> deserializedByID = new HashMap<>();
    IdentityHashMap<SerializedClassifierInstance, ClassifierInstance<?>> serializedToInstanceMap =
        new IdentityHashMap<>();
    sortedSerializedClassifierInstances.stream()
        .forEach(
            n -> {
              ClassifierInstance<?> instantiated = instantiateFromSerialized(n, deserializedByID);
              if (n.getID() != null && deserializedByID.containsKey(n.getID())) {
                throw new IllegalStateException("Duplicate ID found: " + n.getID());
              }
              deserializedByID.put(n.getID(), instantiated);
              serializedToInstanceMap.put(n, instantiated);
            });
    if (sortedSerializedClassifierInstances.size() != serializedToInstanceMap.size()) {
      throw new IllegalStateException(
          "We got "
              + sortedSerializedClassifierInstances.size()
              + " nodes to deserialize, but we deserialized "
              + serializedToInstanceMap.size());
    }
    ClassifierInstanceResolver classifierInstanceResolver =
        new CompositeClassifierInstanceResolver(
            new MapBasedResolver(deserializedByID), this.instanceResolver);
    NodePopulator nodePopulator =
        new NodePopulator(this, classifierInstanceResolver, deserializationStatus);
    serializedClassifierInstances.stream()
        .forEach(
            node -> {
              nodePopulator.populateClassifierInstance(serializedToInstanceMap.get(node), node);
              ClassifierInstance<?> classifierInstance = serializedToInstanceMap.get(node);
              if (unavailableParentPolicy == UnavailableNodePolicy.PROXY_NODES) {
                // For real parents, the parent is not set directly, but it is set indirectly
                // when adding the child to the parent. For proxy nodes instead we need to set
                // the parent explicitly
                ProxyNode proxyParent = deserializationStatus.proxyFor(node.getParentNodeID());
                if (proxyParent != null) {
                  if (classifierInstance instanceof HasSettableParent) {
                    ((HasSettableParent) classifierInstance).setParent(proxyParent);
                  } else {
                    throw new UnsupportedOperationException(
                        "We do not know how to set explicitly the parent of " + classifierInstance);
                  }
                }
              }
              if (classifierInstance instanceof AnnotationInstance) {
                if (node == null) {
                  throw new IllegalStateException(
                      "Dangling annotation instance found (annotated node is null). ");
                }
                Node annotatedNode = (Node) deserializedByID.get(node.getParentNodeID());
                AnnotationInstance annotationInstance = (AnnotationInstance) classifierInstance;
                if (annotatedNode != null) {
                  annotatedNode.addAnnotation(annotationInstance);
                } else {
                  throw new IllegalStateException(
                      "Cannot resolved annotated node " + annotationInstance.getParent());
                }
              }
            });

    // We want the nodes returned to be sorted as the original serializedNodes
    List<ClassifierInstance<?>> nodesWithOriginalSorting =
        serializedClassifierInstances.stream()
            .map(sn -> serializedToInstanceMap.get(sn))
            .collect(Collectors.toList());
    nodesWithOriginalSorting.addAll(deserializationStatus.proxies);
    return nodesWithOriginalSorting;
  }

  private ClassifierInstance<?> instantiateFromSerialized(
      SerializedClassifierInstance serializedClassifierInstance,
      Map<String, ClassifierInstance<?>> deserializedByID) {
    MetaPointer serializedClassifier = serializedClassifierInstance.getClassifier();
    if (serializedClassifier == null) {
      throw new RuntimeException("No metaPointer available for " + serializedClassifierInstance);
    }
    Classifier<?> classifier = getClassifierResolver().resolveClassifier(serializedClassifier);

    // We prepare all the properties values and pass them to instantiator, as it could use them to
    // build the node
    Map<Property, Object> propertiesValues = new HashMap<>();
    serializedClassifierInstance
        .getProperties()
        .forEach(
            serializedPropertyValue -> {
              Property property =
                  classifier.getPropertyByMetaPointer(serializedPropertyValue.getMetaPointer());
              Objects.requireNonNull(
                  property,
                  "Property with metaPointer "
                      + serializedPropertyValue.getMetaPointer()
                      + " not found in classifier "
                      + classifier
                      + ". SerializedNode: "
                      + serializedClassifierInstance);
              Object deserializedValue =
                  primitiveValuesSerialization.deserialize(
                      property.getType(),
                      serializedPropertyValue.getValue(),
                      property.isRequired());
              propertiesValues.put(property, deserializedValue);
            });
    ClassifierInstance<?> classifierInstance =
        getInstantiator()
            .instantiate(
                classifier, serializedClassifierInstance, deserializedByID, propertiesValues);

    // We ensure that the properties values are set correctly. They could already have been set
    // while instantiating the node. If that is the case, we have nothing to do, otherwise we set
    // the values
    propertiesValues
        .entrySet()
        .forEach(
            pv -> {
              Object deserializedValue = pv.getValue();
              Property property = pv.getKey();
              // Avoiding calling setters, in case the value has been already set at construction
              // time

              if (!Objects.equals(
                  deserializedValue, classifierInstance.getPropertyValue(property))) {
                classifierInstance.setPropertyValue(property, deserializedValue);
              }
            });

    return classifierInstance;
  }

  public void registerLanguage(Language language) {
    getClassifierResolver().registerLanguage(language);
    getPrimitiveValuesSerialization().registerLanguage(language);
  }
}
