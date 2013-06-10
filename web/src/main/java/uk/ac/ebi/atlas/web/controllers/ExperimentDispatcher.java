/*
 * Copyright 2008-2013 Microarray Informatics Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the Gene Expression Atlas project, including source code,
 * downloads and documentation, please see:
 *
 * http://gxa.github.com/gxa
 */

package uk.ac.ebi.atlas.web.controllers;

import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.ebi.atlas.commons.ExperimentResolver;
import uk.ac.ebi.atlas.model.Experiment;
import uk.ac.ebi.atlas.model.cache.baseline.BaselineExperimentsCache;
import uk.ac.ebi.atlas.model.cache.differential.RnaSeqDiffExperimentsCache;
import uk.ac.ebi.atlas.model.cache.microarray.MicroarrayExperimentsCache;
import uk.ac.ebi.atlas.model.differential.microarray.MicroarrayExperiment;
import uk.ac.ebi.atlas.web.ApplicationProperties;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Set;

/**
 * Overview:
 * this is a proxy router / interceptor controller that makes up for the lack of workflow handling mechanisms in Spring MVC (HandlerInterceptors are a very poor thing).
 * It intercepts requests for any resource or sub-resource related to any experiment
 * and implements the following responsibilities:
 * <p/>
 * experiment lookup / experiment resolver
 * - lookup the experiment across different caches
 * -- if there is no experiment for the given accession then http response is routed to 404 - resource not fount
 * view decorator (to keep delegated controllers DRY)
 * - add the experiment to the HttpServletRequest, for any delegated controller to use it
 * - add model attributes that are required by all experiment related views
 * (i.e. required by layout elements that are shared by all experiment views)
 * proxy router for specialized controllers
 * - resolve experiment type (baseline or differential)
 * - forward to the original request URI,
 * but adding a "type = baseline" | "type=differential" http parameter.
 * This last step enables routing to different delegated controllers depending on the type of the requested experiment.
 * Each controller will have to specify params="type=baseline" or params="type=differential" or params="type" in order
 * to handle baseline experiments, differential experiments, or both.
 * <p/>
 * Note: the original query string - request.getQueryString() - is not re-appended to the forwarded request URI
 * because Spring MVC processes and transforms it into ModelAttribute(s) that will be
 * automatically / transparently available to delegated controller,
 * without any need for controller to access the query string.
 * <p/>
 * If more complex request mappings are required (i.e. url exclusions) this post may help:
 * http://stackoverflow.com/questions/8998419/requestmapping-annotation-in-spring-mvc
 * For a Spring MVC reference:
 * http://static.springsource.org/spring/docs/3.2.x/spring-framework-reference/html/mvc.html
 */

@Controller
public final class ExperimentDispatcher {

    private static final String TSV_FILE_EXTENSION = ".tsv";

    public static final String EXPERIMENT_ATTRIBUTE = "experiment";
    public static final String PROTEIN_IDENTIFIER_ATTRIBUTE = "uniprotAccession";

    private static final String ALL_SPECIES_ATTRIBUTE = "allSpecies";
    private static final String ALL_ARRAY_DESIGNS_ATTRIBUTE = "allArrayDesigns";
    private static final String IS_TWO_COLOUR = "isTwoColour";
    private static final String EXPERIMENT_DESCRIPTION_ATTRIBUTE = "experimentDescription";
    private static final String HAS_EXTRA_INFO_ATTRIBUTE = "hasExtraInfo";
    private static final String EXPERIMENT_TYPE_ATTRIBUTE = "type";

    private BaselineExperimentsCache baselineExperimentsCache;
    private RnaSeqDiffExperimentsCache rnaSeqDiffExperimentsCache;
    private MicroarrayExperimentsCache microarrayExperimentsCache;
    private ApplicationProperties applicationProperties;

    private ExperimentResolver experimentResolver;

    @Inject
    private ExperimentDispatcher(BaselineExperimentsCache baselineExperimentsCache, RnaSeqDiffExperimentsCache rnaSeqDiffExperimentsCache,
                                 MicroarrayExperimentsCache microarrayExperimentsCache, ApplicationProperties applicationProperties, ExperimentResolver experimentResolver) {
        this.baselineExperimentsCache = baselineExperimentsCache;
        this.rnaSeqDiffExperimentsCache = rnaSeqDiffExperimentsCache;
        this.microarrayExperimentsCache = microarrayExperimentsCache;
        this.applicationProperties = applicationProperties;
        this.experimentResolver = experimentResolver;
    }

    @RequestMapping(value = {"/experiments/{experimentAccession}",
            "/experiments/{experimentAccession}/*"})
    public String dispatch(HttpServletRequest request, @PathVariable String experimentAccession, Model model) {

        Experiment experiment = getExperiment(experimentAccession, model);

        prepareModel(request, model, experiment);

        String requestURL = getRequestURL(request);

        return "forward:" + requestURL + "?type=" + experiment.getType();
    }

    @RequestMapping(value = "/widgets/heatmap/protein")
    public String dispatchWidget(HttpServletRequest request,
                                 @RequestParam(value = "geneQuery", required = true) String uniprotAccession,
                                 @RequestParam(value = "propertyType", required = false) String propertyType,
                                 Model model) {

        Experiment experiment = getExperiment(experimentResolver.getExperimentAccessionByProperty(uniprotAccession, propertyType), model);
        prepareModel(request, model, experiment);
        String requestURL = getRequestURL(request);

        return "forward:" + requestURL + "?type=" + experiment.getType();

    }

    private String getRequestURL(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestURI = request.getRequestURI();

        return StringUtils.substringAfter(requestURI, contextPath);
    }

    private void prepareModel(HttpServletRequest request, Model model, Experiment experiment) {
        request.setAttribute(EXPERIMENT_ATTRIBUTE, experiment);

        Set<String> allSpecies = experiment.getSpecies();

        model.addAttribute(EXPERIMENT_TYPE_ATTRIBUTE, experiment.getType());

        model.addAttribute(ALL_SPECIES_ATTRIBUTE, StringUtils.join(allSpecies, ", "));

        model.addAttribute(EXPERIMENT_DESCRIPTION_ATTRIBUTE, experiment.getDescription());

        model.addAttribute(HAS_EXTRA_INFO_ATTRIBUTE, experiment.hasExtraInfoFile());
    }

    //ToDo: refactor - create different methods for diff experiment types
    Experiment getExperiment(String experimentAccession, Model model) {
        if (applicationProperties.getBaselineExperimentsIdentifiers().contains(experimentAccession)) {
            return baselineExperimentsCache.getExperiment(experimentAccession);
        }
        if (applicationProperties.getDifferentialExperimentsIdentifiers().contains(experimentAccession)) {
            return rnaSeqDiffExperimentsCache.getExperiment(experimentAccession);
        }
        if (applicationProperties.getMicroarrayExperimentsIdentifiers().contains(experimentAccession)) {
            MicroarrayExperiment microarrayExperiment = microarrayExperimentsCache.getExperiment(experimentAccession);

            model.addAttribute(ALL_ARRAY_DESIGNS_ATTRIBUTE, microarrayExperiment.getArrayDesignAccessions());
            model.addAttribute(IS_TWO_COLOUR, microarrayExperiment.isTwoColour());

            return microarrayExperiment;
        }
        throw new ResourceNotFoundException("Experiment not found with accession: " + experimentAccession);

    }


    //ToDo: this method maybe should go somewhere else, indeed it is required by both Baseline and Differential Query Page Controllers
    public static String buildDownloadURL(HttpServletRequest request) {
        //It's important that here we use the original query string, not the forwarded one
        String originalQueryString = ((HttpServletRequest) ((HttpServletRequestWrapper) request).getRequest()).getQueryString();
        return Joiner.on("?").skipNulls()
                .join(new String[]{request.getRequestURI() + TSV_FILE_EXTENSION, originalQueryString}).toString();
    }

}
