package uk.ac.ebi.atlas.model.experiment;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ebi.atlas.model.DescribesDataColumns;
import uk.ac.ebi.atlas.species.Species;
import uk.ac.ebi.atlas.utils.ExperimentInfo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/*
 * The displayName is a bit confusing - it's used for baseline landing page and I think only there.
 * There's also a title which is fetched from ArrayExpress or (as fallback) from the IDF file.
*/
public abstract class Experiment<DataColumnDescriptor extends DescribesDataColumns> implements Serializable {

    private ExperimentType type;
    protected ExperimentDesign experimentDesign;
    private Species species;
    private List<String> pubMedIds;
    private String accession;
    protected String description;
    private String displayName;
    private String disclaimer;
    private Date lastUpdate;
    private List<String> dataProviderURL;
    private List<String> dataProviderDescription;
    private List<String> alternativeViews;
    private List<String> alternativeViewDescriptions;
    private final ImmutableMap<String, DataColumnDescriptor> dataColumnDescriptorsPerId;
    private final ExperimentDisplayDefaults experimentDisplayDefaults;

    public Experiment(ExperimentType type, String accession, Date lastUpdate, String displayName, String description,
                      String disclaimer, Species species, Collection<String> pubMedIds,
                      ExperimentDesign experimentDesign, List<String> dataProviderURL,
                      List<String> dataProviderDescription, List<String> alternativeViews,
                      List<String> alternativeViewDescriptions, List<DataColumnDescriptor> dataColumnDescriptors,
                      ExperimentDisplayDefaults experimentDisplayDefaults) {

        this.type = type;
        this.lastUpdate = lastUpdate;
        this.experimentDesign = experimentDesign;
        this.accession = accession;
        this.displayName = displayName;
        this.description = description;
        this.disclaimer = disclaimer;
        this.species = species;
        this.pubMedIds = ImmutableList.copyOf(Sets.newTreeSet(pubMedIds));
        this.dataProviderURL = dataProviderURL;
        this.dataProviderDescription = dataProviderDescription;
        this.alternativeViews = alternativeViews;
        this.alternativeViewDescriptions = alternativeViewDescriptions;
        ImmutableMap.Builder<String, DataColumnDescriptor> builder = ImmutableMap.builder();
        for(DataColumnDescriptor dataColumnDescriptor: dataColumnDescriptors){
            builder.put(dataColumnDescriptor.getId(), dataColumnDescriptor);
        }
        this.dataColumnDescriptorsPerId = builder.build();
        this.experimentDisplayDefaults = experimentDisplayDefaults;

    }

    public List<DataColumnDescriptor> getDataColumnDescriptors(){
        return ImmutableList.<DataColumnDescriptor>builder().addAll(dataColumnDescriptorsPerId.values()).build();
    }

    public DataColumnDescriptor getDataColumnDescriptor(String id){
        return dataColumnDescriptorsPerId.get(id);
    }

    public ExperimentType getType() {
        return type;
    }
    
    public ExperimentDesign getExperimentDesign() {
        return experimentDesign;
    }

    public ExperimentDisplayDefaults getDisplayDefaults() {
        return experimentDisplayDefaults;
    }

    public String getDisplayName() {
        if (StringUtils.isNotBlank(displayName)) {
            return displayName;
        }
        return getAccession();
    }

    public String getDescription() {
        return description;
    }

    public String getAccession() {
        return accession;
    }

    public Species getSpecies(){
        return species;
    }

    public String getDisclaimer(){
        return disclaimer;
    }

    public Date getLastUpdate(){
        return lastUpdate;
    }

    public List<String> getPubMedIds(){
        return pubMedIds;
    }

    public List<Pair<String, String>> alternativeViews(){
        List<Pair<String, String>> result = new ArrayList<>();
        Preconditions.checkState(alternativeViews.size() == alternativeViewDescriptions.size());

        for(int i = 0 ; i < alternativeViews.size() ; i++) {
            result.add(Pair.of(alternativeViews.get(i), alternativeViewDescriptions.get(i)));
        }
        return result;
    }

    public ImmutableSet<String> getAnalysedAssays() {
        return ImmutableSet.copyOf(
                getDataColumnDescriptors().stream()
                        .flatMap(dataColumnDescriptor ->
                                    dataColumnDescriptor.assaysAnalyzedForThisDataColumn().stream())
                        .collect(Collectors.toSet()));
    }

    public HashMap<String, Object> getAttributes(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("experimentAccession", accession);
        result.put("experimentDescription", description);
        result.put("type", type);
        result.putAll(species.getAttributes());
        result.put("pubMedIds", pubMedIds);
        result.put("disclaimer", disclaimer);

        //Internet says keywords are not that useful for SEO any more. Remove if it causes you problems.
        List<String> keywords = ImmutableList.<String>builder()
                .add("experiment")
                .add(accession)
                .addAll(dataProviderDescription)
                .addAll(Arrays.asList(type.getDescription().split("_")))
                .addAll(experimentDesign.getAssayHeaders())
                .build();
        result.put("pageKeywords", Joiner.on(',').join(keywords));

        //We want this to show up in Google searches.
        result.put("pageDescription", description);

        // Extra information to show on experiment page (if they were provided in <expAcc>-factors.xml file)
        result.put("dataProviderURL", dataProviderURL);
        result.put("dataProviderDescription", dataProviderDescription);
        result.put("alternativeViews", alternativeViews);
        result.put("alternativeViewDescriptions", alternativeViewDescriptions);

        return result;
    }

    public ExperimentInfo buildExperimentInfo(){
        ExperimentInfo experimentInfo = new ExperimentInfo();
        experimentInfo.setExperimentAccession(accession);
        experimentInfo.setLastUpdate(new SimpleDateFormat("dd-MM-yyyy").format(lastUpdate));
        experimentInfo.setExperimentDescription(description);
        experimentInfo.setSpecies(species.getName());
        experimentInfo.setKingdom(species.getKingdom());
        experimentInfo.setExperimentType(type.getParent());
        experimentInfo.setExperimentalFactors(experimentDesign.getFactorHeaders());
        experimentInfo.setNumberOfAssays(getAnalysedAssays().size());
        return experimentInfo;
    }

    public ImmutableCollection<ImmutableMap<String, String>> getGenomeBrowsers() {
        return type.isMicroRna() ? ImmutableList.of() : species.getGenomeBrowsers();
    }

    public ImmutableCollection<String> getGenomeBrowserNames() {
        if (type.isMicroRna()) {
            return ImmutableList.of();
        }

        ImmutableList.Builder<String> genomeBrowserNamesBuilder = ImmutableList.builder();
        for (ImmutableMap<String, String> genomeBrowser : getGenomeBrowsers()) {
            genomeBrowserNamesBuilder.add(genomeBrowser.get("name"));
        }
        return genomeBrowserNamesBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Experiment<?> that = (Experiment<?>) o;
        return com.google.common.base.Objects.equal(accession, that.accession);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(accession);
    }

    protected abstract JsonObject propertiesForAssay(String runOrAssay);

    public abstract JsonArray groupingsForHeatmap();

}
