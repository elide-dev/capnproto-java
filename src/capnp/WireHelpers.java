package capnp;

class WireHelpers {

    public static StructReader readStructPointer(SegmentReader segment,
                                                 WirePointer ref,
                                                 int nestingLimit) {

        // TODO error handling

        WordPointer ptr = ref.target();
        StructPointer structPtr = (StructPointer)ref;
        int dataSizeWords = structPtr.dataSize();

        return new StructReader(segment,
                                ptr.offset * 8,
                                (ptr.offset + dataSizeWords) * 8,
                                dataSizeWords * 64,
                                structPtr.ptrCount(),
                                (byte)0,
                                nestingLimit - 1);

    }


    public static ListReader readListPointer(SegmentReader segment,
                                             WirePointer ref,
                                             byte expectedElementSize,
                                             int nestingLimit) {

        // TODO check for null, follow fars, nestingLimit

        ListPointer listPtr = (ListPointer)ref;

        WordPointer ptr = ref.target();

        switch (listPtr.elementSize()) {
        case FieldSize.INLINE_COMPOSITE : {
            int wordCount = listPtr.inlineCompositeWordCount();

            WirePointer tag = new WirePointer(ptr);
            ptr.offset += 1;

            // TODO bounds check

            int size = tag.inlineCompositeListElementCount();
            StructPointer structPtr = (StructPointer)tag;
            int wordsPerElement = structPtr.wordSize();

            // TODO check that elemements do not overrun word count

            // TODO check whether the size is compatible

            return new ListReader(segment,    // TODO follow fars
                                  ptr.offset * 8, //
                                  size,
                                  wordsPerElement * 64,
                                  structPtr.dataSize() * 64,
                                  structPtr.ptrCount(),
                                  nestingLimit - 1);
        }
        case FieldSize.VOID : break;
        default :
            throw new Error("unrecognized element size");
        }

        throw new Error();
    }

    public static Text.Reader readTextPointer(SegmentReader segment,
                                              WirePointer ref) {
        ref.target();
        ListPointer listPtr = (ListPointer)ref;
        return new Text.Reader(listPtr);
    }
}
