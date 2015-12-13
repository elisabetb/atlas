"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var $ = require('jquery');
var jQuery = $;
require('jquery.browser');

var URI = require('urijs');

//*------------------------------------------------------------------*

var DifferentialFacetsTree = require('./differential-facets-tree.jsx');
var DifferentialResults = require('./differential-results.jsx');

//*------------------------------------------------------------------*

/**
 * @param {Object} options
 * @param {string} options.facetsContainer - id of the facets container, i.e. a <div> id
 * @param {string} options.resultsContainer - id of the results container, i.e. a <div> id
 * @param {string} options.identifier
 * @param {string} options.queryType - "gene", "geneSet"
 * @param {string} options.species
 */
module.exports = function (options) {

    var ie9 = $.browser.msie && $.browser.version < 10;
    !ie9 && window.addEventListener("popstate", backButtonListener, false);

    var facetsElement = document.getElementById(options.facetsContainer),
        resultsElement = document.getElementById(options.resultsContainer),
        host = window.location.host;

    var query = {
        geneQuery : options.identifier,
        queryType : options.queryType,
        select    : {}
    };

    var differentialFacetsPath = "";
    var differentialResultsPath = "";
    switch (query.queryType) {
        case "gene":
            differentialFacetsPath = "gxa/new/genes/" + query.geneQuery + "/differentialFacets.json";
            differentialResultsPath = "gxa/new/genes/" + query.geneQuery + "/differentialResults.json";
            break;
        case "geneSet":
            differentialFacetsPath = "gxa/new/genesets/" + query.geneQuery + "/differentialFacets.json";
            differentialResultsPath = "gxa/new/genesets/" + query.geneQuery + "/differentialResults.json";
            break;
        default:
            break;
    }

    var facetsTreeData,
        resultsData;
    $.ajaxSetup({ traditional:true });
    $.ajax({
        url: new URI({protocol: URI(window.location).protocol(), hostname: host, path: differentialFacetsPath}).normalize(),
        dataType: 'json',
        success: function(response) {
            facetsTreeData = response;
            if (options.species) {
                for (var i = 0 ; i < facetsTreeData.species.length ; i++) {
                    if (facetsTreeData.species[i].name === options.species) {
                        addSelection(query.select, "species", options.species);
                    }
                }
            }
            if (Object.keys(query.select).length === 0) {
                parseSelectedFacetsFromLocation();
            }
            pushQueryIntoBrowserHistory(true);

            $.ajax({
                url: new URI({protocol: URI(window.location).protocol(), hostname: host, path: differentialResultsPath}).normalize(),
                dataType: 'json',
                success: function(response) {
                    resultsData = response;

                    filterAndRenderResults();
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    console.log("ERROR");
                    console.log("Status: " + textStatus);
                    console.log("Error thrown: " + errorThrown);
                }
            });
        },
        error: function(jqXHR, textStatus, errorThrown) {
            console.log("ERROR");
            console.log("Status: " + textStatus);
            console.log("Error thrown: " + errorThrown);
        }
    });


    function backButtonListener() {
        if (window.location.hash === "#differential") {
            parseSelectedFacetsFromLocation();
            filterAndRenderResults();
        }
    }

    /**
     * Parse the `ds` search field in the URL and assign it to the `query` object
     */
    function parseSelectedFacetsFromLocation() {
        var currentURL = new URI(window.location);
        var selectString = currentURL.search(true)["ds"];
        query.select = selectString ? JSON.parse(selectString) : {};
    }


    function setChecked(checked, facet, facetItem) {
        if (checked) {
            addSelection(query.select, facet, facetItem);
        } else {
            removeSelection(query.select, facet, facetItem);
        }
        pushQueryIntoBrowserHistory(false);
        filterAndRenderResults(facetItem);
    }

    function addSelection(select, facet, facetItem) {
        if (!select[facet]) {
            select[facet] = {};
        }
        select[facet][facetItem] = true;
    }

    function removeSelection(select, facet, facetItem) {
        select[facet][facetItem] = false;
    }

    /**
     * Stringify the `query` object, assign it to the `ds` search field in the URL and store it in the History
     * @param {boolean} replace - use `replaceState` instead of `pushState`
     */
    function pushQueryIntoBrowserHistory(replace) {
        var newURL = new URI(window.location).search(function(data) {
            data.ds = JSON.stringify(query.select);
        });

        if (!ie9) {
            if (replace) {
                history.replaceState(null, "", newURL);
            } else {
                history.pushState(null, "", newURL);
            }
        }
    }



    function filterAndRenderResults(facetItemChecked) {

        var MAX_RESULTS = 1000;
        var count = 0;

        var filteredResults = resultsData.results.filter(function(result) {

            if (count >= MAX_RESULTS) {
                return false;
            }

            for (var facetName in query.select) {
                if (query.select.hasOwnProperty(facetName)) {

                    var hasCheckedItemsInThisFacet = false;
                    var facetMatch = false;

                    for (var facetItem in query.select[facetName]) {
                        if (query.select[facetName].hasOwnProperty(facetItem)) {

                            if (query.select[facetName][facetItem]) {
                                hasCheckedItemsInThisFacet = true;

                                if (result[facetName].constructor === Array) {
                                    facetMatch = facetMatch || result[facetName].indexOf(facetItem) != -1;
                                }
                                else {
                                    facetMatch = facetMatch || result[facetName] === facetItem;
                                }
                            }

                        }
                    }

                    if (hasCheckedItemsInThisFacet && !facetMatch) {
                        return false;
                    }

                }
            }

            count += 1;
            return true;
        });

        var disabledCheckedFacets = {};
        var disabledUncheckedFacets = {};

        for (var facet in facetsTreeData) {
            var facetItems = facetsTreeData[facet];

            var facetsInCommon = {};
            var facetValue = [];
            var facetNotExistsValue = [];

            for (var item in facetItems) {
                var facetItem = facetItems[item];
                var facetWithNoResults = false;
                var sameValue = true;

                for (var index in filteredResults) {
                    var filtered = filteredResults[index];
                    var facetResults = filtered[facet];
                    var factorValue = [];

                    //If the facetResults contains an array of facets in common
                    if(facet === "factors" && facetResults.length > 1) {
                        factorValue.push(facetResults);
                        if (!facetsInCommon.hasOwnProperty(facet)) {
                            facetsInCommon[facet] = factorValue;
                        }

                        for(var index in facetResults) {
                            var _facetItem = facetResults[index];
                            if (_facetItem.toString() !== facetItem.name) {
                                sameValue = false;
                            }
                            if (_facetItem.toString() === facetItem.name)  {
                                facetWithNoResults = true;
                            }
                        }
                    }

                    else {
                        if (!facetsInCommon.hasOwnProperty(facet)) { //if is empty the first time
                            facetValue.push(facetResults.toString());
                            facetsInCommon[facet] = facetValue;
                            facetValue = [];
                        }

                        if (facetsInCommon.hasOwnProperty(facet) && facetResults.toString() !== facetItem.name) {
                            sameValue = false;
                        }
                        if (facetResults.toString() === facetItem.name)  {
                            facetWithNoResults = true;
                        }
                    }
                }
                //check and grey out facets that has results
                if (sameValue) {
                    if(facetValue.indexOf(facetResults.toString()) === -1) {
                        facetValue.push(facetResults.toString());
                    }
                }
                //unchecked and grey out facetItems with no results
                if (!facetWithNoResults) {
                    if(facetNotExistsValue.indexOf(facetItem.name) === -1) {
                        facetNotExistsValue.push(facetItem.name);
                    }
                }
            }
            if (facetValue.length > 0 && facetItemChecked != facetValue.toString()) {
                disabledCheckedFacets[facet] = facetValue;
            }
            if (facetNotExistsValue.length > 0) {
                disabledUncheckedFacets[facet] = facetNotExistsValue;
            }
        }

        React.render(
            React.createElement(
                DifferentialFacetsTree, {facets: facetsTreeData, checkedFacets: query.select, setChecked: setChecked,
                    disabledCheckedFacets: disabledCheckedFacets, disabledUncheckedFacets: disabledUncheckedFacets}
            ),
            facetsElement
        );

        React.render(
            React.createElement(
                DifferentialResults,
                {results: filteredResults, maxDownLevel: resultsData.maxDownLevel, minDownLevel: resultsData.minDownLevel, minUpLevel: resultsData.minUpLevel, maxUpLevel: resultsData.maxUpLevel}
            ),
            resultsElement
        );
    }
};
