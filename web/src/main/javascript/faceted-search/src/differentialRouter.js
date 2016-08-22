"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var ReactDOM = require('react-dom');

var $ = require('jquery');

var url = require('url');
var querystring = require('querystring');

//*------------------------------------------------------------------*

var DifferentialTab = require('./DifferentialTab.jsx');
var pushQueryIntoBrowserHistory = require('./urlMaintainer.js').differential;

//*------------------------------------------------------------------*

/**
 * @param {Object} options
 * @param {string} options.target - id of the container
 * @param {string} options.atlasHost
 * @param {string} options.identifier
 * @param {string} options.geneQuery
 * @param {string} options.conditionQuery
 * @param {string} options.species
 */
module.exports = function (options) {

    var host = options.atlasHost ? options.atlasHost : window.location.host;

    var query = {select: {}};

    var differentialFacetsUrlObject = {protocol: window.location.protocol, host: host},
        differentialResultsUrlObject = {protocol: window.location.protocol, host: host};

    if (window.location.pathname.match(/\/genes\//)) {
        differentialFacetsUrlObject.pathname = 'gxa/json/genes/' + options.identifier + '/differentialFacets';
        differentialResultsUrlObject.pathname = 'gxa/json/genes/' + options.identifier + '/differentialResults';
    } else if (window.location.pathname.match(/\/genesets\//)) {
        queryParams = {organism: options.species};
        differentialFacetsUrlObject.pathname = 'gxa/json/genesets/' + options.identifier + '/differentialFacets';
        differentialFacetsUrlObject.query = queryParams;
        differentialResultsUrlObject.pathname = 'gxa/json/genesets/' + options.identifier + '/differentialResults';
        differentialFacetsUrlObject.query = queryParams;
    } else {
        var queryParams = {geneQuery: options.geneQuery, conditionQuery: options.conditionQuery, organism: options.species};
        differentialFacetsUrlObject.pathname = 'gxa/json/query/differentialFacets';
        differentialFacetsUrlObject.query = queryParams;
        differentialResultsUrlObject.pathname = 'gxa/json/query/differentialResults';
        differentialResultsUrlObject.query = queryParams;
    }

    var facetsTreeData,
        resultsData;
    $.ajaxSetup({ traditional:true });
    $.ajax({
        url: url.format(differentialFacetsUrlObject),
        dataType: "json",
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
                query.select = parseSelectedFacetsFromLocation();
            }
            pushQueryIntoBrowserHistory(query.select,true);

            $.ajax({
                url: url.format(differentialResultsUrlObject),
                dataType: "json",
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

    /**
     * Parse the `ds` search field in the URL
     */
    function parseSelectedFacetsFromLocation() {
        var currentURL = url.parse(window.location.toString());
        var selectString = querystring.parse(currentURL.query)["ds"];
        return selectString ? JSON.parse(selectString) : {};
    }


    function setChecked(checked, facet, facetItem) {
        if (checked) {
            addSelection(query.select, facet, facetItem);
        } else {
            removeSelection(query.select, facet, facetItem);
        }
        pushQueryIntoBrowserHistory(query.select, false);
        filterAndRenderResults();
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

    function filterResults(resultsData, query) {
        return resultsData.results.filter(function(result) {
            for (var facetName in query) {
                if (query.hasOwnProperty(facetName)) {

                    var hasCheckedItemsInThisFacet = false;
                    var facetMatch = false;

                    for (var facetItem in query[facetName]) {
                        if (query[facetName].hasOwnProperty(facetItem)) {

                            if (query[facetName][facetItem]) {
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

            return true;
        });

    }

    function filterAndRenderResults() {

        var filteredResults = filterResults (resultsData, query.select);

        var disabledCheckedFacets = {};
        var disabledUncheckedFacets = {};

        var selectedFacets = [];
        for (var facet in query.select) {
            var itemFacet = query.select[facet];
            for (var item in itemFacet) {
                if (itemFacet[item]) {
                    selectedFacets.push(item);
                }
            }
        }

        var notSelectedFacets = {};
        for (var facet in facetsTreeData) {
            var facetItems = facetsTreeData[facet];
            var unSelectedFacets = [];
            for (var item in facetItems) {
                var facetItem = facetItems[item];
                if (selectedFacets.indexOf(facetItem.name) === -1) {
                    unSelectedFacets.push(facetItem);
                }
            }
            notSelectedFacets[facet] = unSelectedFacets;
        }

        for (var facet in notSelectedFacets) {
            var facetNotExistsValue = [];
            var _query = query.select;

            for (var item in notSelectedFacets[facet]) {
                var facetItem = notSelectedFacets[facet][item];

                if (_query.hasOwnProperty(facet)) {
                    _query[facet][facetItem.name] = true;
                } else {
                    var _item = {};
                    _item[facetItem.name] = true;
                    _query[facet] = _item;
                }

                var queryResults = filterResults(resultsData, _query);
                if (queryResults.length === 0 || queryResults.length === filteredResults.length) {
                    facetNotExistsValue.push(facetItem.name);
                }
                _query[facet][facetItem.name] = false; // once calculated, leave it as it was
            }

            if (facetNotExistsValue.length > 0) {
                disabledUncheckedFacets[facet] = facetNotExistsValue;
            }
        }

        for (var facet in disabledUncheckedFacets) {
            var facetValue = [];

            for (var item in disabledUncheckedFacets[facet]) {
                var existsFacet = true;
                var existMultiValueFacet = false;

                for (var index in filteredResults) {
                    var filtered = filteredResults[index];
                    var facetResults = filtered[facet];

                    if (facet === "factors" && facetResults.length > 1 ) {
                        if (facetResults.indexOf(disabledUncheckedFacets[facet][item].toString()) === -1) {
                            existsFacet = false;
                        } else {
                            existMultiValueFacet = true;
                        }

                    } else if (disabledUncheckedFacets[facet][item].toString() !== facetResults.toString()) {
                        existsFacet = false;
                    }
                }

                //All the results contain the same item
                if (existsFacet) {
                    facetValue.push(disabledUncheckedFacets[facet][item]);
                }

                //If all the results don't contain the same facet but the factor multiValue exists in more than one result
                if (!existsFacet && existMultiValueFacet) {
                    disabledUncheckedFacets[facet].splice(item, 1);
                }
            }

            //FacetValue contains all the items that are present in the results for a facet
            if (facetValue.length > 0) {
                disabledCheckedFacets[facet] = facetValue;
            }
        }

        ReactDOM.render(
          React.createElement(
            DifferentialTab, {
              facets: {facets: facetsTreeData, checkedFacets: query.select, setChecked: setChecked,
                  disabledCheckedFacets: disabledCheckedFacets, disabledUncheckedFacets: disabledUncheckedFacets},
              results: {results: filteredResults.slice(0, 1000), maxDownLevel: resultsData.maxDownLevel,
                  minDownLevel: resultsData.minDownLevel, minUpLevel: resultsData.minUpLevel,
                  maxUpLevel: resultsData.maxUpLevel, host: host}
            }
          ), document.getElementById(options.target || "gxaDifferentialTab")
        )
    }
};
