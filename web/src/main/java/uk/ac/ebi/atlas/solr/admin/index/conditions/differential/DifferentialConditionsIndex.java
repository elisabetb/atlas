package uk.ac.ebi.atlas.solr.admin.index.conditions.differential;

import org.apache.solr.client.solrj.SolrClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.model.differential.DifferentialExperiment;
import uk.ac.ebi.atlas.solr.admin.index.conditions.ConditionsIndex;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Scope("prototype")
public class DifferentialConditionsIndex extends ConditionsIndex<DifferentialExperiment> {

    @Inject
    public DifferentialConditionsIndex(@Qualifier("differentialConditionsSolrClient") SolrClient solrClient, DifferentialConditionsBuilder propertiesBuilder) {
        super(solrClient, propertiesBuilder);
    }
}
