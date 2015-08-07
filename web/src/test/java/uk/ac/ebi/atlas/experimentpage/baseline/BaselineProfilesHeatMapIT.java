package uk.ac.ebi.atlas.experimentpage.baseline;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import uk.ac.ebi.atlas.experimentpage.context.BaselineRequestContext;
import uk.ac.ebi.atlas.experimentpage.context.BaselineRequestContextBuilder;
import uk.ac.ebi.atlas.experimentpage.context.GenesNotFoundException;
import uk.ac.ebi.atlas.experimentpage.context.LoadGeneIdsIntoRequestContext;
import uk.ac.ebi.atlas.model.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.baseline.BaselineProfilesList;
import uk.ac.ebi.atlas.model.baseline.Factor;
import uk.ac.ebi.atlas.profiles.baseline.BaselineProfileStreamOptions;
import uk.ac.ebi.atlas.profiles.baseline.BaselineProfileStreamOptionsWrapperAsGeneSets;
import uk.ac.ebi.atlas.trader.cache.BaselineExperimentsCache;
import uk.ac.ebi.atlas.web.BaselineRequestPreferences;
import uk.ac.ebi.atlas.web.GeneQuery;

import javax.annotation.Resource;
import javax.inject.Inject;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {"classpath:applicationContext.xml", "classpath:solrContextIT.xml", "classpath:oracleContext.xml"})
public class BaselineProfilesHeatMapIT {

    public static final String E_MTAB_513 = "E-MTAB-513";
    private static final Factor FACTOR_LEUKOCYTE = new Factor("ORGANISM_PART", "leukocyte");

    @Resource(name = "baselineProfilesHeatMap")
    private BaselineProfilesHeatMap subject;

    @Inject
    private BaselineExperimentsCache baselineExperimentsCache;

    @Inject
    BaselineRequestContextBuilder baselineRequestContextBuilder;

    private BaselineRequestPreferences requestPreferences = new BaselineRequestPreferences();

    private BaselineRequestContext baselineRequestContext;

    @Inject
    private LoadGeneIdsIntoRequestContext loadGeneIdsIntoRequestContext;

    @Before
    public void initRequestContext() {
        populateRequestContext();
    }

    private void populateRequestContext() {
        BaselineExperiment baselineExperiment = baselineExperimentsCache.getExperiment(E_MTAB_513);

        requestPreferences.setQueryFactorType("ORGANISM_PART");
        baselineRequestContext =
                baselineRequestContextBuilder.forExperiment(baselineExperiment)
                .withPreferences(requestPreferences)
                .build();
    }

    // http://localhost:8080/gxa/experiments/E-MTAB-513?displayLevels=true&specific=true
    @Test
    public void eMTAB513_Specific() throws GenesNotFoundException {
        BaselineProfilesList profiles = subject.fetch(baselineRequestContext);

        assertThat(profiles.getTotalResultCount(), is(146));
        // System.out.println(Joiner.on("\", \"").join(profiles.extractGeneNames()));
        assertThat(profiles.extractGeneNames(), contains("ACTL7A", "TEX33", "SLC10A1", "DCAF4L2", "ABCG8", "PRMT8", "UBQLNL", "FAM47B", "CCNE2", "ZSCAN5B", "WFDC10A", "USP26", "SBK2", "LIPF", "SRSF2", "C17orf64", "SCN2A", "RP11-192H23.4", "MYOD1", "ESPL1", "ZNF713", "NOTUM", "CRISP3", "C1QL3", "GFI1", "MT-ATP6", "SLAMF6", "MFSD7", "PVRL4", "RTDR1", "ARHGAP8", "RAB27B", "TRPM2", "IL12RB2", "DAPL1", "RGS7BP", "PRSS16", "SRGAP3", "C1QTNF2", "NEDD8", "PMM2", "RGP1", "GLB1L2", "CCDC66", "RANBP17", "BICD1", "ZFP2", "NEBL", "COL15A1", "APOBR"));

    }

    //http://localhost:8080/gxa/experiments/E-MTAB-513?displayLevels=true&_specific=on
    @Test
    public void eMTAB513_NotSpecific() throws GenesNotFoundException {
        setNotSpecific();
        //requestPreferences.setHeatmapMatrixSize(170);
        //populateRequestContext();
        BaselineProfilesList profiles = subject.fetch(baselineRequestContext);

        assertThat(profiles.getTotalResultCount(), is(146));
        // System.out.println(Joiner.on("\", \"").join(profiles.extractGeneNames()));
        assertThat(profiles.extractGeneNames(), contains("MT-ATP6", "TMSB10", "RTN4", "NEDD8", "ARPC5", "RAB13", "CTNNA1", "ATP1B3", "CALU", "VPS4A", "PPT1", "VTI1B", "S1PR1", "AIDA", "ZCRB1", "IL13RA1", "ARHGAP1", "CBX1", "CRLS1", "BMI1", "PLEKHB2", "BOD1", "MAPRE2", "POLR2B", "NEBL", "SNRPA", "POLE3", "GFPT1", "B3GNT1", "GGPS1", "MRPL13", "DEPTOR", "VAPB", "COL15A1", "C6orf203", "GOLPH3L", "PTBP3", "TUBA1C", "SLK", "ASNS", "EMCN", "OCEL1", "RNF25", "RNF41", "INPP5D", "ERLIN1", "CRY2", "THOC3", "COL4A3BP", "RAE1"));
    }

    // http://localhost:8080/gxa/experiments/E-MTAB-513?displayLevels=true&specific=true&queryFactorValues=leukocyte
    @Test
    public void eMTAB513_Specific_QueryFactorLeukocyte() throws GenesNotFoundException {
        setQueryFactor(FACTOR_LEUKOCYTE);

        BaselineProfilesList profiles = subject.fetch(baselineRequestContext);

        assertThat(profiles.getTotalResultCount(), is(19));
        assertThat(profiles.extractGeneNames(), contains("APOBR", "INPP5D", "PTBP3", "ZDHHC18", "PPT1", "ARPC5", "GFI1", "INPP4A", "SNX27", "KDM4A", "POLR2B", "PLCG2", "SLAMF6", "COL4A3BP", "RAB27B", "FBXO38", "C6orf1", "TMSB10", "POLE3"));

    }

    // http://localhost:8080/gxa/experiments/E-MTAB-513?displayLevels=true&_specific=on&queryFactorValues=leukocyte
    @Test
    public void eMTAB513_NotSpecific_QueryFactorLeukocyte() throws GenesNotFoundException {
        setNotSpecific();
        setQueryFactor(FACTOR_LEUKOCYTE);

        BaselineProfilesList profiles = subject.fetch(baselineRequestContext);

        assertThat(profiles.getTotalResultCount(), is(102));
        // System.out.println(Joiner.on("\", \"").join(profiles.extractGeneNames()));
        assertThat(profiles.extractGeneNames(), contains("MT-ATP6", "TMSB10", "ARPC5", "NEDD8", "RTN4", "PPT1", "INPP5D", "S1PR1", "PTBP3", "ATP1B3", "PLEKHB2", "POLR2B", "CTNNA1", "MAPRE2", "SLAMF6", "IL13RA1", "APOBR", "ZDHHC18", "ARHGAP1", "POLE3", "AIDA", "VPS4A", "CBX1", "COL4A3BP", "PLCG2", "GGPS1", "BMI1", "RNF41", "SNX27", "INPP4A", "TUBA1C", "ZCRB1", "SNRPA", "OCEL1", "FBXO38", "VRK3", "VTI1B", "RNF25", "RAE1", "CALU", "SLK", "C6orf1", "MRPL13", "CRLS1", "MSH6", "BET1", "TERF2", "VAPB", "KDM4A", "BOD1"));
    }

    // http://localhost:8080/gxa/experiments/E-MTAB-513?displayLevels=true&_specific=on&geneQuery=react_14797+react_19184+react_604+react_111102+react_111217+react_6900+react_71+react_116125+react_75774+react_6802+react_17015+react_22258+react_15518+react_115566+react_12627&geneSetMatch=true
    @Test
    public void eMTAB513_NotSpecific_MultipleGeneSets_Order() throws GenesNotFoundException {
        setNotSpecific();
        setGeneQuery("react_14797\treact_19184\treact_604\treact_111102\treact_111217\treact_6900\treact_71\treact_116125\treact_75774\treact_6802\treact_17015\treact_22258\treact_15518\treact_115566\treact_12627");

        BaselineProfilesList profiles = subject.fetch(asGeneSet(baselineRequestContext));

        assertThat(profiles.getTotalResultCount(), is(15));
        // System.out.println(Joiner.on("\", \"").join(profiles.extractGeneNames()));
        assertThat(profiles.extractGeneNames(), contains("react_111217", "react_6802", "react_19184", "react_111102", "react_604", "react_14797", "react_6900", "react_15518", "react_116125", "react_17015", "react_75774", "react_22258", "react_71", "react_115566", "react_12627"));
    }

    private BaselineProfileStreamOptions asGeneSet(BaselineRequestContext baselineRequestContext) throws GenesNotFoundException {
        loadGeneIdsIntoRequestContext.load(baselineRequestContext, baselineRequestContext.getFilteredBySpecies());
        return new BaselineProfileStreamOptionsWrapperAsGeneSets(baselineRequestContext);
    }

    // http://localhost:8080/gxa/experiments/E-MTAB-513?displayLevels=true&specific=true&geneQuery=react_14797+react_19184+react_604+react_111102+react_111217+react_6900+react_71+react_116125+react_75774+react_6802+react_17015+react_22258+react_15518+react_115566+react_12627&geneSetMatch=true
    @Test
    public void eMTAB513_Specific_MultipleGeneSets_Order() throws GenesNotFoundException {
        setGeneQuery("react_14797\treact_19184\treact_604\treact_111102\treact_111217\treact_6900\treact_71\treact_116125\treact_75774\treact_6802\treact_17015\treact_22258\treact_15518\treact_115566\treact_12627");

        BaselineProfilesList profiles = subject.fetch(asGeneSet(baselineRequestContext));

        assertThat(profiles.getTotalResultCount(), is(15));
        // System.out.println(Joiner.on("\", \"").join(profiles.extractGeneNames()));
        assertThat(profiles.extractGeneNames(), contains("react_111217", "react_6802", "react_19184", "react_111102", "react_604", "react_14797", "react_6900", "react_15518", "react_116125", "react_17015", "react_75774", "react_22258", "react_71", "react_115566", "react_12627"));
    }

    private void setGeneQuery(String geneQuery) {
        requestPreferences.setGeneQuery(GeneQuery.create(geneQuery));
    }

    private void setNotSpecific() {
        requestPreferences.setSpecific(false);
    }

    private void setQueryFactor(Factor factor) {
        requestPreferences.setQueryFactorType(factor.getType());
        requestPreferences.setQueryFactorValues(Collections.singleton(factor.getValue()));

        // set query factor on request context
        populateRequestContext();
    }

}
