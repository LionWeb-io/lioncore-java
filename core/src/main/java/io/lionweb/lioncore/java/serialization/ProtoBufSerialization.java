package io.lionweb.lioncore.java.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.lionweb.lioncore.java.language.LionCoreBuiltins;
import io.lionweb.lioncore.java.model.ClassifierInstance;
import io.lionweb.lioncore.java.model.impl.ProxyNode;
import io.lionweb.lioncore.java.self.LionCore;
import io.lionweb.lioncore.java.serialization.data.*;
import io.lionweb.lioncore.java.serialization.data.MetaPointer;
import io.lionweb.lioncore.protobuf.*;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProtoBufSerialization extends AbstractSerialization {

    /**
     * This has specific support for LionCore or LionCoreBuiltins.
     */
    public static ProtoBufSerialization getStandardSerialization() {
        ProtoBufSerialization serialization = new ProtoBufSerialization();
        serialization.classifierResolver.registerLanguage(LionCore.getInstance());
        serialization.instantiator.registerLionCoreCustomDeserializers();
        serialization.primitiveValuesSerialization
                .registerLionBuiltinsPrimitiveSerializersAndDeserializers();
        serialization.instanceResolver.addAll(LionCore.getInstance().thisAndAllDescendants());
        serialization.instanceResolver.addAll(LionCoreBuiltins.getInstance().thisAndAllDescendants());
        return serialization;
    }

    /**
     * This has no specific support for LionCore or LionCoreBuiltins.
     */
    public static ProtoBufSerialization getBasicSerialization() {
        ProtoBufSerialization serialization = new ProtoBufSerialization();
        return serialization;
    }

    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(byte[] bytes) throws IOException {
        return deserializeToNodes(new ByteArrayInputStream(bytes));
    }

    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(File file) throws IOException {
        return deserializeToNodes(new FileInputStream(file));
    }

    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(InputStream inputStream) throws IOException {
        return deserializeToNodes(Chunk.parseFrom(inputStream));
    }

    public List<io.lionweb.lioncore.java.model.Node> deserializeToNodes(Chunk chunk) {
        return deserializeToClassifierInstances(chunk).stream()
                .filter(ci -> ci instanceof io.lionweb.lioncore.java.model.Node)
                .map(ci -> (io.lionweb.lioncore.java.model.Node) ci)
                .collect(Collectors.toList());
    }

    public List<ClassifierInstance<?>> deserializeToClassifierInstances(Chunk chunk) {
        SerializedChunk serializationBlock =
                deserializeSerializationChunk(chunk);
        validateSerializationBlock(serializationBlock);
        return deserializeSerializationBlock(serializationBlock);
    }

    private SerializedChunk deserializeSerializationChunk(Chunk chunk) {
        Map<Integer, String> stringsMap = new HashMap<>();
        for (int i = 0; i < chunk.getStringValuesCount(); i++) {
            stringsMap.put(i, chunk.getStringValues(i));
        }
        Map<Integer, MetaPointer> metapointersMap = new HashMap<>();
        for (int i = 0; i < chunk.getMetaPointersCount(); i++) {
            io.lionweb.lioncore.protobuf.MetaPointer mp = chunk.getMetaPointers(i);
            MetaPointer metaPointer = new MetaPointer();
            metaPointer.setKey(stringsMap.get(mp.getKey()));
            metaPointer.setLanguage(stringsMap.get(mp.getLanguage()));
            metaPointer.setVersion(stringsMap.get(mp.getVersion()));
            metapointersMap.put(i, metaPointer);
        };

        SerializedChunk serializedChunk = new SerializedChunk();
        serializedChunk.setSerializationFormatVersion(chunk.getSerializationFormatVersion());
        chunk.getLanguagesList().forEach(l -> {
            UsedLanguage usedLanguage = new UsedLanguage();
            usedLanguage.setKey(stringsMap.get(l.getKey()));
            usedLanguage.setVersion(stringsMap.get(l.getVersion()));
            serializedChunk.addLanguage(usedLanguage);
        });

        chunk.getNodesList().forEach(n -> {
            SerializedClassifierInstance sci = new SerializedClassifierInstance();
            sci.setID(stringsMap.get(n.getId()));
            sci.setParentNodeID(stringsMap.get(n.getParent()));
            sci.setClassifier(metapointersMap.get(n.getClassifier()));
            n.getPropertiesList().forEach(p -> {
                SerializedPropertyValue spv = new SerializedPropertyValue();
                spv.setValue(stringsMap.get(p.getValue()));
                spv.setMetaPointer(metapointersMap.get(p.getMetaPointerIndex()));
                sci.addPropertyValue(spv);
            });
            n.getContainmentsList().forEach(c -> {
                SerializedContainmentValue scv = new SerializedContainmentValue();
                scv.setValue(c.getChildrenList().stream().map(el -> stringsMap.get(el)).collect(Collectors.toList()));
                scv.setMetaPointer(metapointersMap.get(c.getMetaPointerIndex()));
                sci.addContainmentValue(scv);
            });
            n.getReferencesList().forEach(r -> {
                SerializedReferenceValue srv = new SerializedReferenceValue();
                r.getValuesList().forEach(rv -> {
                    SerializedReferenceValue.Entry entry = new SerializedReferenceValue.Entry();
                    entry.setReference(stringsMap.get(rv.getReferred()));
                    entry.setResolveInfo(stringsMap.get(rv.getResolveInfo()));
                    srv.addValue(entry);
                });
                srv.setMetaPointer(metapointersMap.get(r.getMetaPointerIndex()));
                sci.addReferenceValue(srv);
            });
            // TODO
//          n.getAnnotationsList().forEach(a -> {
//              sci.getAnnotations().add(a);
//          });
            serializedChunk.addClassifierInstance(sci);
        });
        return serializedChunk;
    }

    public byte[] serializeTreesToByteArray(ClassifierInstance<?>... roots) {
        Set<String> nodesIDs = new HashSet<>();
        List<ClassifierInstance<?>> allNodes = new ArrayList<>();
        for (ClassifierInstance<?> root : roots) {
            Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
            ClassifierInstance.collectSelfAndDescendants(root, true, classifierInstances);
            classifierInstances.forEach(
                    n -> {
                        // We support serialization of incorrect nodes, so we allow nodes without ID to be
                        // serialized
                        if (n.getID() != null) {
                            if (!nodesIDs.contains(n.getID())) {
                                allNodes.add(n);
                                nodesIDs.add(n.getID());
                            }
                        } else {
                            allNodes.add(n);
                        }
                    });
        }
        return serializeNodesToByteArray(
                allNodes.stream().filter(n -> !(n instanceof ProxyNode)).collect(Collectors.toList()));
    }

    public byte[] serializeNodesToByteArray(List<ClassifierInstance<?>> classifierInstances) {
        if (classifierInstances.stream().anyMatch(n -> n instanceof ProxyNode)) {
            throw new IllegalArgumentException("Proxy nodes cannot be serialized");
        }
        SerializedChunk serializationBlock = serializeNodesToSerializationBlock(classifierInstances);
        return serializeToByteArray(serializationBlock);
    }

    public byte[] serializeNodesToByteArray(ClassifierInstance<?>... classifierInstances) {
        return serializeNodesToByteArray(Arrays.asList(classifierInstances));
    }

    public byte[] serializeToByteArray(SerializedChunk serializedChunk) {
        return serialize(serializedChunk).toByteArray();
    }

    private class SerializeHelper {
        final Map<MetaPointer, Integer> metaPointers = new HashMap<>();
        final Map<String, Integer> strings = new HashMap<>();

        int stringIndexer(String string) {
            if (string == null) {
                return -1;
            }
            if (strings.containsKey(string)) {
                return strings.get(string);
            }
            int index = strings.size();
            strings.put(string, index);
            return index;
        }

        ;

        int metaPointerIndexer(MetaPointer metaPointer) {
            if (metaPointers.containsKey(metaPointer)) {
                return metaPointers.get(metaPointer);
            }
            io.lionweb.lioncore.protobuf.MetaPointer metaPointerDef =
                    io.lionweb.lioncore.protobuf.MetaPointer.newBuilder()
                            .setKey(stringIndexer(metaPointer.getKey()))
                            .setVersion(stringIndexer(metaPointer.getVersion()))
                            .setLanguage(stringIndexer(metaPointer.getLanguage()))
                            .build();
            int index = metaPointers.size();
            metaPointers.put(metaPointer, index);
            return index;
        }

        Node serializeNode(SerializedClassifierInstance n) {
            Node.Builder nodeBuilder = Node.newBuilder();
            nodeBuilder.setId(this.stringIndexer(n.getID()));
            nodeBuilder.setClassifier(this.metaPointerIndexer((n.getClassifier())));
            nodeBuilder.setParent(this.stringIndexer(n.getParentNodeID()));
            // TODO n.getAnnotations()
            n.getProperties()
                    .forEach(
                            p -> {
                                Property.Builder b = Property.newBuilder();
                                b.setValue(this.stringIndexer(p.getValue()));
                                b.setMetaPointerIndex(this.metaPointerIndexer((p.getMetaPointer())));
                                nodeBuilder.addProperties(b.build());
                            });
            n.getContainments()
                    .forEach(
                            p ->
                                    nodeBuilder.addContainments(
                                            Containment.newBuilder()
                                                    .addAllChildren(p.getValue().stream().map(v -> this.stringIndexer(v)).collect(Collectors.toList()))
                                                    .setMetaPointerIndex(
                                                            this.metaPointerIndexer((p.getMetaPointer())))
                                                    .build()));
            n.getReferences()
                    .forEach(
                            p ->
                                    nodeBuilder.addReferences(
                                            Reference.newBuilder()
                                                    .addAllValues(
                                                            p.getValue().stream()
                                                                    .map(
                                                                            rf -> {
                                                                                ReferenceValue.Builder b =
                                                                                        ReferenceValue.newBuilder();
                                                                                b.setReferred(this.stringIndexer(rf.getReference()));
                                                                                b.setResolveInfo(this.stringIndexer(rf.getResolveInfo()));
                                                                                return b.build();
                                                                            })
                                                                    .collect(Collectors.toList()))
                                                    .setMetaPointerIndex(
                                                            this.metaPointerIndexer((p.getMetaPointer())))
                                                    .build()));
            return nodeBuilder.build();
        }
    }



  public BulkImport serializeBulkImport(List<BulkImportElement> elements) {
    BulkImport.Builder bulkImportBuilder = BulkImport.newBuilder();
      ProtoBufSerialization.SerializeHelper serializeHelper = new ProtoBufSerialization.SerializeHelper();

    elements.forEach(
        bulkImportElement -> {
          io.lionweb.lioncore.protobuf.BulkImportElement.Builder bulkImportElementBuilder =
              io.lionweb.lioncore.protobuf.BulkImportElement.newBuilder();
          bulkImportElementBuilder.setMetaPointerIndex(
                  serializeHelper.metaPointerIndexer(bulkImportElement.containment));
          SerializedChunk serializedChunk =
              serializeTreeToSerializationBlock(bulkImportElement.tree);

          serializedChunk
              .getClassifierInstances()
              .forEach(
                  n -> {
                    Node.Builder nodeBuilder = Node.newBuilder();
                    nodeBuilder.setId(serializeHelper.stringIndexer(n.getID()));
                    nodeBuilder.setClassifier(serializeHelper.metaPointerIndexer((n.getClassifier())));
                    nodeBuilder.setParent(serializeHelper.stringIndexer(n.getParentNodeID()));
                    // TODO n.getAnnotations()
                    n.getProperties()
                        .forEach(
                            p -> {
                              Property.Builder b = Property.newBuilder();
                              b.setValue(serializeHelper.stringIndexer(p.getValue()));
                              b.setMetaPointerIndex(serializeHelper.metaPointerIndexer((p.getMetaPointer())));
                              nodeBuilder.addProperties(b.build());
                            });
                    n.getContainments()
                        .forEach(
                            p ->
                                nodeBuilder.addContainments(
                                    Containment.newBuilder()
                                        .addAllChildren(p.getValue().stream().map(v -> serializeHelper.stringIndexer(v)).collect(Collectors.toList()))
                                        .setMetaPointerIndex(
                                            serializeHelper.metaPointerIndexer((p.getMetaPointer())))
                                        .build()));
                    n.getReferences()
                        .forEach(
                            p ->
                                nodeBuilder.addReferences(
                                    Reference.newBuilder()
                                        .addAllValues(
                                            p.getValue().stream()
                                                .map(
                                                    rf -> {
                                                      ReferenceValue.Builder b =
                                                          ReferenceValue.newBuilder();
                                                      b.setReferred(serializeHelper.stringIndexer(rf.getReference()));
                                                        b.setResolveInfo(serializeHelper.stringIndexer(rf.getResolveInfo()));
                                                      return b.build();
                                                    })
                                                .collect(Collectors.toList()))
                                        .setMetaPointerIndex(
                                            serializeHelper.metaPointerIndexer((p.getMetaPointer())))
                                        .build()));
                    bulkImportElementBuilder.addTree(nodeBuilder.build());
                  });

          bulkImportBuilder.addElements(bulkImportElementBuilder.build());
        });

    serializeHelper.metaPointers
        .entrySet()
            .stream().sorted()
        .forEach(
            entry ->
                bulkImportBuilder.addMetaPointerDefs(
                    io.lionweb.lioncore.protobuf.MetaPointer.newBuilder()
                        .setLanguage(serializeHelper.stringIndexer(entry.getKey().getLanguage()))
                        .setKey(serializeHelper.stringIndexer(entry.getKey().getKey()))
                        .setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()))
                        .build()));
    return bulkImportBuilder.build();
  }

  public Chunk serializeTree(ClassifierInstance<?> classifierInstance) {
    if (classifierInstance instanceof ProxyNode) {
      throw new IllegalArgumentException("Proxy nodes cannot be serialized");
    }
    Set<ClassifierInstance<?>> classifierInstances = new LinkedHashSet<>();
    ClassifierInstance.collectSelfAndDescendants(classifierInstance, true, classifierInstances);

    SerializedChunk serializedChunk =
        serializeNodesToSerializationBlock(
            classifierInstances.stream()
                .filter(n -> !(n instanceof ProxyNode))
                .collect(Collectors.toList()));
    return serialize(serializedChunk);
  }

  public Chunk serialize(SerializedChunk serializedChunk) {
    Chunk.Builder chunkBuilder = Chunk.newBuilder();
    chunkBuilder.setSerializationFormatVersion(serializedChunk.getSerializationFormatVersion());
    SerializeHelper serializeHelper = new SerializeHelper();
    serializedChunk
        .getLanguages()
        .forEach(
            ul -> {
              chunkBuilder.addLanguages(
                  Language.newBuilder()
                          .setKey(serializeHelper.stringIndexer(ul.getKey()))
                          .setVersion(serializeHelper.stringIndexer(ul.getVersion()))
                          .build());
            });

    serializedChunk
        .getClassifierInstances()
        .forEach(
            n -> chunkBuilder.addNodes(serializeHelper.serializeNode(n)));

    serializeHelper.strings.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
        chunkBuilder.addStringValues(entry.getKey());
    });
      serializeHelper.metaPointers.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(entry -> {
          io.lionweb.lioncore.protobuf.MetaPointer.Builder metaPointer = io.lionweb.lioncore.protobuf.MetaPointer.newBuilder();
          metaPointer.setKey(serializeHelper.stringIndexer(entry.getKey().getKey()));
          metaPointer.setLanguage(serializeHelper.stringIndexer(entry.getKey().getLanguage()));
          metaPointer.setVersion(serializeHelper.stringIndexer(entry.getKey().getVersion()));
          chunkBuilder.addMetaPointers(metaPointer.build());
      });
    return chunkBuilder.build();
  }
}