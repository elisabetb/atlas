/*
 * Copyright 2008-2016 Gene Expression Team, EMBL-European Bioinformatics Institute
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
 * Expression Atlas:
 * https://www.ebi.ac.uk/gxa
 *
 * For further details of the Expression Atlas project, including source code,
 * downloads and documentation, please see:
 * https://github.com/gxa/atlas
 */

/**
 * Created by Alfonso Muñoz-Pomer Fuentes <amunoz@ebi.ac.uk> on 04/03/2016.
 */

package uk.ac.ebi.atlas.experimentimport.coexpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import uk.ac.ebi.atlas.trader.ExperimentTrader;
import uk.ac.ebi.atlas.web.controllers.ResourceNotFoundException;

import javax.inject.Inject;
import java.io.IOException;

@Deprecated //moved to ExperimentAdminController
@Controller
@Scope("request")
@RequestMapping("/admin")
public class BaselineCoexpressionProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaselineCoexpressionProfileController.class);

    private BaselineCoexpressionProfileLoader baselineCoexpressionProfileLoader;
    private ExperimentTrader experimentTrader;

    @Inject
    public BaselineCoexpressionProfileController(BaselineCoexpressionProfileLoader baselineCoexpressionProfileLoader,
                                                 ExperimentTrader experimentTrader) {
        this.baselineCoexpressionProfileLoader = baselineCoexpressionProfileLoader;
        this.experimentTrader = experimentTrader;
    }

    @RequestMapping(value = "/importAllCoexpressionProfiles", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String importAllCoexpressionsProfiles() {

        StringBuilder stringBuilder = new StringBuilder();
        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());

        for (String experimentAccession : experimentTrader.getBaselineExperimentAccessions()) {
            try {
                stopWatch.start(experimentAccession);

                int count = baselineCoexpressionProfileLoader.loadBaselineCoexpressionsProfile(experimentAccession);

                stopWatch.stop();

                stringBuilder.append(String.format("%s: %,d coexpression profiles loaded in %s seconds\n", experimentAccession, count, stopWatch.getLastTaskTimeMillis() / 1000.0));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                stringBuilder.append(String.format("%s: there was an error updating coexpression profiles; see the logs for more details \n", experimentAccession));
            } finally {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
            }
        }

        return stringBuilder.toString();
    }


    @RequestMapping(value = "/importCoexpressionProfile", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String importCoexpressionProfile(@RequestParam("accession") String experimentAccession) {
        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());
        stopWatch.start();

        int count = baselineCoexpressionProfileLoader.loadBaselineCoexpressionsProfile(experimentAccession);

        stopWatch.stop();

        return String.format("%s: %,d coexpression profiles loaded in %s seconds", experimentAccession, count, stopWatch.getTotalTimeSeconds());
    }

    @RequestMapping(value = "/deleteAllCoexpressionProfiles", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteAllCoexpressionProfiles() {

        StringBuilder stringBuilder = new StringBuilder();
        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());

        for (String experimentAccession : experimentTrader.getBaselineExperimentAccessions()) {
            try {
                stopWatch.start(experimentAccession);

                int count = baselineCoexpressionProfileLoader.deleteCoexpressionsProfile(experimentAccession);

                stopWatch.stop();

                stringBuilder.append(String.format("%s: %,d coexpression profiles deleted in %s seconds\n", experimentAccession, count, stopWatch.getLastTaskTimeMillis() / 1000.0));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                stringBuilder.append(String.format("%s: there was an error updating coexpression profiles; see the logs for more details \n", experimentAccession));
            } finally {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
            }
        }

        return stringBuilder.toString();
    }

    @RequestMapping(value = "/deleteCoexpressionProfile", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String deleteCoexpressionProfile(@RequestParam("accession") String experimentAccession) {
        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());
        stopWatch.start();

        int count = baselineCoexpressionProfileLoader.deleteCoexpressionsProfile(experimentAccession);

        stopWatch.stop();

        return String.format("%s: %,d coexpression profiles deleted in %s seconds", experimentAccession, count, stopWatch.getTotalTimeSeconds());
    }

    @RequestMapping(value = "/updateAllCoexpressionProfiles", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String updateAllCoexpressionProfiles() {
        StringBuilder stringBuilder = new StringBuilder();

        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());
        for (String experimentAccession : experimentTrader.getBaselineExperimentAccessions()) {
            try {
                stopWatch.start(experimentAccession);

                int deleteCount = baselineCoexpressionProfileLoader.deleteCoexpressionsProfile(experimentAccession);
                int loadCount = baselineCoexpressionProfileLoader.loadBaselineCoexpressionsProfile(experimentAccession);

                stopWatch.stop();

                stringBuilder.append(String.format("%s: %,d coexpression profiles deleted and %,d coexpression profiles loaded in %s seconds\n", experimentAccession, deleteCount, loadCount, stopWatch.getLastTaskTimeMillis() / 1000.0));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                stringBuilder.append(String.format("%s: there was an error updating coexpression profiles; see the logs for more details \n", experimentAccession));
            } finally {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
            }
        }

        return stringBuilder.toString();
    }

    @RequestMapping(value = "/updateCoexpressionProfile", produces = "text/plain;charset=UTF-8")
    @ResponseBody
    public String updateCoexpressionProfile(@RequestParam("accession") String experimentAccession) {
        StopWatch stopWatch = new StopWatch(getClass().getSimpleName());
        stopWatch.start();

        int deleteCount = baselineCoexpressionProfileLoader.deleteCoexpressionsProfile(experimentAccession);
        int loadCount = baselineCoexpressionProfileLoader.loadBaselineCoexpressionsProfile(experimentAccession);

        stopWatch.stop();

        return String.format("%s: %,d coexpression profiles deleted and %,d coexpression profiles loaded in %s seconds", experimentAccession, deleteCount, loadCount, stopWatch.getTotalTimeSeconds());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String handleException(Exception e) throws IOException {
        String lineSeparator = "<br>";
        LOGGER.error(e.getMessage(), e);

        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage()).append(lineSeparator);

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append(lineSeparator);
        }

        return sb.toString();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleResourceNotFoundException(Exception e) throws IOException {
        LOGGER.error(e.getMessage(), e);
        return e.getClass().getSimpleName() + ": " + e.getMessage();
    }

}