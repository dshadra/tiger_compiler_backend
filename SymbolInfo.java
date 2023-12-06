public class SymbolInfo {

    enum Storage {
        STATIC,
        LOCAL,
        FUNCTIONPARAM
    }
    String symbol;
    String type;
    Integer offset;
    String baseLabel;

    Storage storage;

    String stackAreaAddressLabel;

    int dimensions;

    public int getDimensions() {
        return dimensions;
    }

    public void setDimensions(int dimensions) {
        this.dimensions = dimensions;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getStackAreaAddressLabel() {
        return stackAreaAddressLabel;
    }

    public void setStackAreaAddressLabel(String stackAreaAddressLabel) {
        this.stackAreaAddressLabel = stackAreaAddressLabel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getBaseLabel() {
        return baseLabel;
    }

    public void setBaseLabel(String baseLabel) {
        this.baseLabel = baseLabel;
    }

    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
}
