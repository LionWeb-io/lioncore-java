// automatically generated by the FlatBuffers compiler, do not modify

package io.lionweb.lioncore.java.serialization.flatbuffers;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public final class FBProperty extends Table {
  public static void ValidateVersion() { Constants.FLATBUFFERS_24_3_25(); }
  public static FBProperty getRootAsFBProperty(ByteBuffer _bb) { return getRootAsFBProperty(_bb, new FBProperty()); }
  public static FBProperty getRootAsFBProperty(ByteBuffer _bb, FBProperty obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public FBProperty __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int metaPointerIndex() { int o = __offset(4); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public int value() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }

  public static int createFBProperty(FlatBufferBuilder builder,
      int metaPointerIndex,
      int value) {
    builder.startTable(2);
    FBProperty.addValue(builder, value);
    FBProperty.addMetaPointerIndex(builder, metaPointerIndex);
    return FBProperty.endFBProperty(builder);
  }

  public static void startFBProperty(FlatBufferBuilder builder) { builder.startTable(2); }
  public static void addMetaPointerIndex(FlatBufferBuilder builder, int metaPointerIndex) { builder.addInt(0, metaPointerIndex, 0); }
  public static void addValue(FlatBufferBuilder builder, int value) { builder.addInt(1, value, 0); }
  public static int endFBProperty(FlatBufferBuilder builder) {
    int o = builder.endTable();
    return o;
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public FBProperty get(int j) { return get(new FBProperty(), j); }
    public FBProperty get(FBProperty obj, int j) {  return obj.__assign(__indirect(__element(j), bb), bb); }
  }
}

