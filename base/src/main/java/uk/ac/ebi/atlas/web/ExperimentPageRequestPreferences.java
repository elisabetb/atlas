package uk.ac.ebi.atlas.web;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.hibernate.validator.constraints.Range;
import uk.ac.ebi.atlas.model.ExpressionUnit;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.Set;

public abstract class ExperimentPageRequestPreferences<Unit extends ExpressionUnit> extends SearchRequest {

    public static final int DEFAULT_NUMBER_OF_RANKED_GENES = 50;

    private static final int HEATMAP_SIZE_MIN = 0;

    private static final int HEATMAP_SIZE_MAX = 1000;

    private double cutoff = getDefaultCutoff();

    @NotNull
    @Range(min = HEATMAP_SIZE_MIN, max = HEATMAP_SIZE_MAX)
    private Integer heatmapMatrixSize = DEFAULT_NUMBER_OF_RANKED_GENES;

    private boolean specific = true;

    protected ExperimentPageRequestPreferences() {
    }

    Set<String> selectedColumnIds = Collections.emptySet();

    @SuppressWarnings("unused")
    public void setSelectedColumnIds(Set<String> selectedColumnIds){
        this.selectedColumnIds = (selectedColumnIds == null) ? Collections.<String>emptySet() : ImmutableSet.copyOf(selectedColumnIds);
    }

    public Set<String> getSelectedColumnIds(){
        return selectedColumnIds;
    }

    public Integer getHeatmapMatrixSize() {
        return this.heatmapMatrixSize;
    }

    public void setHeatmapMatrixSize(Integer heatmapMatrixSize) {
        if (heatmapMatrixSize != null) {
            this.heatmapMatrixSize = heatmapMatrixSize;
        }
    }

    public Double getCutoff() {
        return this.cutoff;
    }

    //must be public because the jsp needs to access it
    public abstract double getDefaultCutoff();

    public void setCutoff(Double cutoff) {
        // handle no value case, eg: when textbox is left empty
        if (cutoff != null) {
            this.cutoff = cutoff;
        }
    }

    public boolean isSpecific() {
        return this.specific;
    }

    public void setSpecific(boolean specific) {
        this.specific = specific;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .addValue(super.toString())
                .add("cutoff", cutoff)
                .add("specific", specific)
                .toString();
    }

    public abstract Unit getUnit();

    /*
    Used for equality of cache keys.
    Currently:
    - Kryo serialized files
    - Histograms
    This combined with experiment accession should 1-1 map to a data file.
    A bit of a design wart -
     */
    public String serializationShortString(){
        return getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExperimentPageRequestPreferences that = (ExperimentPageRequestPreferences) o;
        return serializationShortString().equals(that.serializationShortString());
    }

    @Override
    public int hashCode() {
        return serializationShortString().hashCode();
    }

}
