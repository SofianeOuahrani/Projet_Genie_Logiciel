// BitPackingFactory - OUAHRANI KHALDI Sofiane
package gl.bp;

// Factory pour créer et gérer les différentes implémentations de BitPacking.

public class CompressionFactory {

    //identif. des types
    public static final String TYPE_ALIGNED = "ALIGNED";
    public static final String TYPE_OVERLAP = "OVERLAP";
    public static final String TYPE_OVERFLOW = "OVERFLOW";

    public static BitPacking createCompressor(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de compression ne peut pas être nul.");
        }


        switch (type.toUpperCase()) {
            case TYPE_ALIGNED:
                return new BitPackingAligned();

            case TYPE_OVERLAP:
                return new BitPackingOverlap();

            case TYPE_OVERFLOW:
                return new BitPackingOverflow();

            default:
                throw new IllegalArgumentException("Type de compression '" + type + "' inconnu. Utilisez ALIGNED, OVERLAP ou OVERFLOW.");
        }
    }
}
