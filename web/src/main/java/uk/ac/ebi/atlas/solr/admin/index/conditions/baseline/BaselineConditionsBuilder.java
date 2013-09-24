package uk.ac.ebi.atlas.solr.admin.index.conditions.baseline;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Scope;
import uk.ac.ebi.atlas.model.Experiment;
import uk.ac.ebi.atlas.model.baseline.BaselineExperiment;
import uk.ac.ebi.atlas.model.differential.AssayGroup;
import uk.ac.ebi.atlas.solr.admin.index.conditions.Condition;
import uk.ac.ebi.atlas.solr.admin.index.conditions.ConditionsBuilder;

import javax.inject.Named;
import java.util.Collection;
import java.util.Set;

@Named
@Scope("prototype")
public class BaselineConditionsBuilder extends ConditionsBuilder<BaselineExperiment> {
    @Override
    public Collection<Condition> buildProperties(BaselineExperiment experiment) {
        Collection<Condition> conditions = Lists.newLinkedList();

        Set<AssayGroup> assayGroups = experiment.getAssayGroups();
        for (AssayGroup assayGroup : assayGroups) {
            conditions.addAll(buildPropertiesForAssayGroup(experiment, assayGroup));
        }

        return conditions;

    }

    protected Collection<Condition> buildPropertiesForAssayGroup(Experiment experiment, AssayGroup assayGroup) {

        Collection<Condition> conditions = Sets.newHashSet();

        for (String assayAccession : assayGroup) {

            Set<String> values = collectAssayProperties(experiment.getExperimentDesign(), assayAccession);
            Condition condition = new Condition(experiment.getAccession(),
                    assayGroup.getId(), values);
            conditions.add(condition);
        }

        return conditions;
    }

}
