<%--
  ~ Copyright 2008-2012 Microarray Informatics Team, EMBL-European Bioinformatics Institute
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  ~
  ~
  ~ For further details of the Gene Expression Atlas project, including source code,
  ~ downloads and documentation, please see:
  ~
  ~ http://gxa.github.com/gxa
  --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<script language="JavaScript" type="text/javascript"
        src="${pageContext.request.contextPath}/resources/js/searchFormModule.js"></script>


<h2>Expression Atlas, Differential and Baseline Expression</h2>

<%-- <p>Expression Atlas is a semantically enriched database of publicly available gene and transcript expression data.
    The data is re-analysed in-house to detect genes showing interesting baseline and differential expression patterns
	under the conditions of the original experiment. <a href="about.html">Read more about Expression Atlas.</a></p> --%>

<p>The Expression Atlas provides information on gene expression patterns under
    different biological conditions. Gene expression data is re-analysed in-house
    to detect genes showing interesting baseline and differential expression
    patterns. <a href="about.html">Read more about Expression Atlas.</a></p>

<section class="grid_18 alpha">
    <form method="get" action="query" id="searchForm">
        <table class="form-grid">
            <tr>
                <td>
                    <label>Gene query</label>
                    <span data-help-loc="#geneSearch"/>
                </td>
                <%--<td>--%>
                    <%--<label>Organisms</label>--%>
                <%--</td>--%>
                <td>
                    <label>Experimental conditions</label>
                    <span data-help-loc="#experimentalConditions"/>
                </td>
                <td rowspan="2" style="display:table-cell;text-align:center;vertical-align: middle;">
                    <div class="actions">
                        <div>
                            <input id="submit-button" type="submit" value="Search" tabindex="4">
                        </div>
                        <div>
                            <input id="reset-button" type="reset" value="Reset" tabindex="5">
                        </div>
                    </div>
                </td>
            </tr>

            <tr>
                <td>
                    <div style="display:inline-block">
                        <textarea id="geneQuery" name="geneQuery" maxlength="900" rows="2" cols="36"
                                  placeholder="(all genes)" tabindex="1"></textarea>

                        <div>
								<span>E.g. 
									<a href="query?geneQuery=ASPM">ASPM</a>,
									<a href="query?geneQuery=REACT_1619">REACT_1619</a>,
									<a href="query?geneQuery=%22zinc+finger%22">"zinc finger"</a>
								</span>
								
								<span style="float:left">
                                    <input id="exactMatch" name="exactMatch" type="checkbox" value="true"
                                           checked="checked" tabindex="2">
                                    <label for="exactMatch">Exact match</label>
                                    <input type="hidden" name="_exactMatch" value="on">
                                </span>
                        </div>
                    </div>
                </td>
                <%--<td>--%>
                    <%--<div style="display: inline-block">--%>
                        <%--<select></select>--%>

                    <%--</div>--%>
                <%--</td>--%>
                <td>
                    <div style="display:inline-block">
                        <textarea id="condition" name="condition" maxlength="900" rows="2" cols="36"
                                  placeholder="(all conditions)" tabindex="3"></textarea>

                        <div>
							<span class="examples">E.g.
								<a href="query?condition=leaf">leaf</a>,
								<a href="query?condition=Mus+musculus">"Mus musculus"</a>,
								<a href="query?condition=cancer">cancer</a>
							</span>
                        </div>
                    </div>
                </td>
            </tr>
        </table>
    </form>

    <table style="margin-top: 10px;">
        <tr>
            <td><label>Still need the old Expression Atlas? </label></td>
            <td><a href="http://www-test.ebi.ac.uk/gxa">Click here</a></td>
        </tr>
    </table>
</section>

<section class="grid_5 omega">
    <table class="form-grid" style="margin:0px 9px;">
        <tr>
            <td><label>Browse</label></td>
        </tr>
        <tr>
            <td><a href="experiments"><img src="resources/images/experiment_page_small.png">
                All Experiments</a></td>
        </tr>
        <tr>
            <td><a href="baseline/experiments"><img src="resources/images/allup2_transparent_bkg.png">
                Baseline Experiments</a></td>
        </tr>
    </table>
    <br/>
    <div class="form-grid" style="margin:0px 9px;" >
        <div style="margin-bottom:5px;">
            <div><label>Publications</label></div>
        </div>
        <div>
            <div style="display:inline-block; max-width: 50%; vertical-align:top; margin-left:5px;"><img style="max-width:50px; max-height:50px;" src="resources/images/publications_cover.gif"></div>
            <div style="display:inline-block; max-width:70%; vertical-align: middle;"><a href="http://nar.oxfordjournals.org/content/42/D1/D926.full.pdf">Expression Atlas update - a database of gene and transcript expression from microarray and sequencing-based functional genomics experiments.</a>
            (Nucleic Acids Research, 10.1093/nar/gkt1270)</div>
        </div>
        <div>
            <div style="display:inline-block; margin-left:5px;">
                <span><a class="ext" href="http://nar.oxfordjournals.org/content/42/D1/D926.full">HTML</a></span>
                <span><a class="ext" href="http://nar.oxfordjournals.org/content/42/D1/D926.full.pdf">PDF</a></span>
            </div>
        </div>
    </div>

</section>


<%-- placeholder which is loaded with tooltip text --%>
<div id="help-placeholder" style="display: none"></div>

<script type="text/javascript">

    (function ($) { //self invoking wrapper function that prevents $ namespace conflicts

        $(document).ready(function () {
            var $buttons = $('#submit-button, #reset-button'),
                    $searchFields = $('#geneQuery, #condition');

            searchFormModule.geneQuerySearchBoxInitAutocomplete();

            searchFormModule.disableCarriageReturn("#geneQuery");

            searchFormModule.disableCarriageReturn("#condition");

            helpTooltipsModule.init('experiment', '${pageContext.request.contextPath}', '');

            initButtons();

            disableButtonsWhenAllSearchFieldsAreEmpty();

            function initButtons() {
                $buttons.each(function () {
                    $(this).button({ disabled: true });
                });
            }

            function disableButtonsWhenAllSearchFieldsAreEmpty() {
                $searchFields.on('keyup',function () {
                    $buttons.button("option", "disabled", allFieldsEmpty());
                }).keyup();

                function allFieldsEmpty() {
                    var atLeastOneValue = false;
                    $searchFields.each(function () {
                        atLeastOneValue = atLeastOneValue || ($.trim(this.value).length > 0);
                    });
                    return !atLeastOneValue;
                }
            }

        });

    })(jQuery);

</script>
