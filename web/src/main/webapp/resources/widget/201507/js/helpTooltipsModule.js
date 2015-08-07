/*
 * Copyright 2008-2012 Microarray Informatics Team, EMBL-European Bioinformatics Institute
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

/*global $, jQuery, console:false */
var helpTooltipsModule = (function($) {
    "use strict";

    var _contextRoot;

    function buildHelpAnchor() {
        return $("<a/>", {
                    'class': 'help-icon',
                    'href': '#',
                    'title': '',
                    'text': '?'
                });
    }

    function getHelpFileName(pageName){
        return 'help-tooltips.' + pageName + '-page.html';
    }

    function getHelpLocation(pageName, inlineAnchor) {
        return _contextRoot + '/resources/html/' + getHelpFileName(pageName) + ' ' + inlineAnchor;
    }

    function initTooltips(pageName, parentElementId){

        var anchor = buildHelpAnchor();

        var helpSelector = (typeof parentElementId === "object") ? parentElementId : (parentElementId == '') ? "[data-help-loc]" : "#" + parentElementId + " [data-help-loc]";

        $(helpSelector)
            .append(anchor)
            .click(function(e) {
                e.preventDefault();
            })
            .tooltip(
            {
                tooltipClass: "gxaHelpTooltip",
                content: function(callback) {
                    var tooltipHelpHtmlId = $(this).parent().attr('data-help-loc');

                    // load help text into #help-placeholder. NB: page must contain a div with id help-placeholder
                    $("#help-placeholder").load(getHelpLocation(pageName, tooltipHelpHtmlId),
                        function (response, status, xhr) {
                            var tooltipContent;
                            if (status === "error") {
                                tooltipContent = "Sorry but there was an error: " + xhr.status + " " + xhr.statusText;
                                callback(tooltipContent);
                                return;
                            }
                            tooltipContent = $(this).text();
                            if (!tooltipContent){
                                tooltipContent = "Missing help section for id = " + tooltipHelpHtmlId + " in html file " + getHelpFileName(pageName);
                            }
                            callback(tooltipContent);

                        }
                    );

                }
            });

    }

    return {
        init:  function(pageName, contextRoot, parentElementId) {
                    _contextRoot = contextRoot;
                    initTooltips(pageName, parentElementId);
                },
        buildHelpAnchor: buildHelpAnchor,
        getHelpFileName: getHelpFileName
    };

}(jQuery));
