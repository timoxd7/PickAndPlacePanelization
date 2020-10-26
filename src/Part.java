public class Part {
    public Part() {}

    public Part(Part other) {
        designator = other.designator;
        midX = other.midX;
        midY = other.midY;
        layer = other.layer;
        rotation = other.rotation;
        comment = other.comment;
        footprint = other.footprint;
        lcscPart = other.lcscPart;
    }

    public String designator;

    public String getDesignator() {
        return designator;
    }

    public void setDesignator(String designator) {
        this.designator = designator;
    }

    // PnP
    public float midX;

    public float getMidX() {
        return midX;
    }

    public void setMidX(float midX) {
        this.midX = midX;
    }

    public float midY;

    public float getMidY() {
        return midY;
    }

    public void setMidY(float midY) {
        this.midY = midY;
    }

    public String layer = "Top";

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public float rotation;

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    // BOM
    public String comment;

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String footprint;

    public String getFootprint() {
        return footprint;
    }

    public void setFootprint(String footprint) {
        this.footprint = footprint;
    }

    public String lcscPart = "";

    public String getLcscPart() {
        return lcscPart;
    }

    public void setLcscPart(String lcscPart) {
        this.lcscPart = lcscPart;
    }
}
