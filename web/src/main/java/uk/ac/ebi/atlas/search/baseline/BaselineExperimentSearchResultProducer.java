package uk.ac.ebi.atlas.search.baseline;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import uk.ac.ebi.atlas.model.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.baseline.Factor;
import uk.ac.ebi.atlas.model.baseline.FactorGroup;
import uk.ac.ebi.atlas.model.baseline.impl.FactorSet;
import uk.ac.ebi.atlas.trader.ExperimentTrader;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named

public class BaselineExperimentSearchResultProducer {

    private final ExperimentTrader experimentTrader;

    @Inject
    public BaselineExperimentSearchResultProducer(ExperimentTrader experimentTrader) {
        this.experimentTrader = experimentTrader;
    }

    public BaselineExperimentSearchResult buildProfilesForTissueExperiments(List<BaselineExpression> expressions) {
        return buildProfilesForExperiments(expressions, "ORGANISM_PART");

    }

    public BaselineExperimentSearchResult buildProfilesForExperiments(List<BaselineExpression> expressions, String defaultQueryFactorType) {

        ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> expressionsByExperimentSlice = groupByExperimentSlice(expressions);

        ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> tissueExperimentsBySlice = filter(expressionsByExperimentSlice, defaultQueryFactorType);

        SortedSet<Factor> tissueFactorsAcrossAllExperiments = extractAllNonFilterFactors(tissueExperimentsBySlice);

        BaselineExperimentProfilesList profiles = createBaselineExperimentProfiles(tissueExperimentsBySlice, tissueFactorsAcrossAllExperiments, defaultQueryFactorType);

        return new BaselineExperimentSearchResult(profiles, tissueFactorsAcrossAllExperiments);
    }

    private static ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> filter(ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> expressionsByExperimentSlice, String defaultQueryFactorType) {
        ImmutableListMultimap.Builder<BaselineExperimentSlice, BaselineExpression> builder = ImmutableListMultimap.builder();

        for (Map.Entry<BaselineExperimentSlice, Collection<BaselineExpression>> baselineExperimentSliceCollectionEntry : expressionsByExperimentSlice.asMap().entrySet()) {
            BaselineExperiment experiment = baselineExperimentSliceCollectionEntry.getKey().experiment();
            if (experiment.getExperimentalFactors().getDefaultQueryFactorType().equals(defaultQueryFactorType)) {
                builder.putAll(baselineExperimentSliceCollectionEntry.getKey(), baselineExperimentSliceCollectionEntry.getValue());
            }
        }

        return builder.build();
    }

    private static BaselineExperimentProfilesList createBaselineExperimentProfiles(ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> expressionsByExperimentSlice, SortedSet<Factor> tissueFactorsAcrossAllExperiments, String defaultQueryFactorType) {
        BaselineExperimentProfilesList profiles = new BaselineExperimentProfilesList();

        for (BaselineExperimentSlice experimentSlice : expressionsByExperimentSlice.keySet()) {

            BaselineExperiment experiment = experimentSlice.experiment();

            Set<Factor> factorDifference = Sets.difference(tissueFactorsAcrossAllExperiments, experimentSlice.nonFilterFactors());

            BaselineExperimentProfile profile = new BaselineExperimentProfile(experimentSlice);

            for (BaselineExpression baselineExpression : expressionsByExperimentSlice.get(experimentSlice)) {
                uk.ac.ebi.atlas.model.baseline.BaselineExpression expression = createBaselineExpression(experiment, baselineExpression);
                //check expression level string if the factor
                profile.add(defaultQueryFactorType, expression);
            }

            //For the nonFilterFactors which don't have expression, create new expression with NT level
            for (Factor factor : factorDifference) {
                FactorGroup factorGroup = new FactorSet(factor);
                uk.ac.ebi.atlas.model.baseline.BaselineExpression baselineExpression = new uk.ac.ebi.atlas.model.baseline.BaselineExpression("NT", factorGroup);
                profile.add(defaultQueryFactorType, baselineExpression);
            }

            profiles.add(profile);
        }

        Collections.sort(profiles);

        profiles.setTotalResultCount(profiles.size());
        return profiles;
    }

    private SortedSet<Factor> extractAllNonFilterFactors(ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> expressionsByExperimentSlice) {
        ImmutableSortedSet.Builder<Factor> factors = ImmutableSortedSet.naturalOrder();

        for (BaselineExperimentSlice experimentSlice : expressionsByExperimentSlice.keySet()) {
            factors.addAll(experimentSlice.nonFilterFactors());
        }

        return factors.build();
    }


    private ImmutableListMultimap<BaselineExperimentSlice, BaselineExpression> groupByExperimentSlice(List<BaselineExpression> expressions) {
        Function<BaselineExpression, BaselineExperimentSlice> createExperimentSlice = new Function<BaselineExpression, BaselineExperimentSlice>() {
            public BaselineExperimentSlice apply(BaselineExpression input) {
                String experimentAccession = input.experimentAccession();
                String assayGroupId = input.assayGroupId();

                //TODO: this is a bottleneck because it goes back to the database each time - to improve perf,
                //we need to lookup the experiment type from a cache and then get the experiment from the appropriate cachee
                //eg: http://localhost:8080/gxa/widgets/heatmap/baselineAnalytics?geneQuery=ccnt2&species=Homo%20sapiens&source=ORGANISM_PART
                // takes 800ms, should be much faster than this
                BaselineExperiment experiment = (BaselineExperiment) (experimentTrader.getPublicExperiment(experimentAccession));
                FactorGroup filterFactors = experiment.getExperimentalFactors().getNonDefaultFactors(assayGroupId);

                return BaselineExperimentSlice.create(experiment, filterFactors);
            }
        };

        return Multimaps.index(expressions, createExperimentSlice);
    }

    private static uk.ac.ebi.atlas.model.baseline.BaselineExpression createBaselineExpression(BaselineExperiment experiment, BaselineExpression baselineExpression) {
        double level = baselineExpression.expressionLevel();

        String assayGroupId = baselineExpression.assayGroupId();
        FactorGroup factorGroup = experiment.getExperimentalFactors().getFactorGroup(assayGroupId);

        return new uk.ac.ebi.atlas.model.baseline.BaselineExpression(level, factorGroup);
    }


}
