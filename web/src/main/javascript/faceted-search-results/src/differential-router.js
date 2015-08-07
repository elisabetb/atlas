"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var $ = require('jquery');
var jQuery = $;
require('jquery.browser');
var queryString = require('query-string');

//*------------------------------------------------------------------*

var Facets = require('./facets.jsx');
var DifferentialResults = require('./differential-results.jsx');

//*------------------------------------------------------------------*

module.exports = function (facetsElement, resultsElement, facetData) {

    //TODO: add this outside the module, when module is first loaded
    var ie9 = $.browser.msie && $.browser.version < 10;
    if (!ie9) {
        window.addEventListener('popstate', renderPage, false);
    }


    renderPage();

    function renderPage() {
        debugger;
        var locationSearch = window.location.search;
        var query = queryString.parse(locationSearch.slice(1)),
            pathname = window.location.pathname;
        query.select = query.select && JSON.parse(query.select);
        render(query, pathname);
    }

    function render(query, pathname) {

        React.render(React.createElement(Facets, {facets: facetData, checkedFacets: query.select, setChecked: setChecked}),
            facetsElement);

        queryToResults(query.geneQuery, query.select);

        function setChecked(checked, facet, facetItem) {
            var newSelect = checked ? addSelection(query.select, facet, facetItem) : removeSelection(query.select, facet, facetItem);
            var newQueryString = "?geneQuery=" + query.geneQuery + "&select=" + JSON.stringify(newSelect);
            console.log(newQueryString);
            queryToResults(query.geneQuery, newSelect);
            navigateTo(pathname, newQueryString);
        }

        function navigateTo(pathname, newQueryString) {
            debugger;
            var state, title;
            if (ie9) {
                window.location.search = newQueryString;
            } else {
                history.pushState(null, null, pathname + newQueryString);
                renderPage();
            }
        }


        function addSelection(select, facet, facetItem) {
            if (!select) {
                select = {};
            }

            if (!select[facet]) {
                select[facet] = {};
            }
            select[facet][facetItem] = true;
            return select;
        }

        function removeSelection(select, facet, facetItem) {
            select[facet][facetItem] = false;
            return select;
        }
    }

    function queryToResults(geneQuery, query) {
        /*
         query.geneQuery=zinc finger
         query.select= {
         "species": {"homo sapiens":true,"mus musculus":true,"arabidopsis thaliana":true},
         "factors":{"infect":true},
         "numReplicates":{"3":true},
         "experimentType":{"rnaseq_mrna_differential":true},
         "kingdom":{"ensembl":true},
         "regulation":{"UP":true}
         }
         */
        var species = [];
        var experimentType = [];
        var kingdom = [];
        var factors = [];
        var numReplicates = [];
        var regulation;

        var select = query;
        for (var facets in select) {
            if (select.hasOwnProperty(facets)) {
                var items = select[facets];

                for (var key in items) {
                    if (items.hasOwnProperty(key)) {
                        var value = items[key];
                        if (facets == "species" && value == true) {
                            species.push(key);
                        } else if (facets == "factors" && value == true) {
                            factors.push(key);
                        } else if (facets == "experimentType" && value == true) {
                            experimentType.push(key);
                        } else if (facets == "kingdom" && value == true) {
                            kingdom.push(key);
                        } else if (facets == "numReplicates" && value == true) {
                            numReplicates.push(key);
                        } else if (facets == "regulation" && value == true) {
                            regulation = key;
                        }
                    }
                }
            }
        }

        $.ajaxSetup({ traditional:true });

        $.ajax({
            url: window.location.pathname + '/json',
            data: {
                'geneQuery': geneQuery,
                'species': species.toString(),
                'experimentType': experimentType.toString(),
                'kingdom': kingdom.toString(),
                'factors': factors.toString(),
                'numReplicates': numReplicates.toString(),
                'regulation': regulation
            },
            dataType: 'json',
            success: function(response) {
                React.render(
                    React.createElement(DifferentialResults, {diffResultsData: $.parseJSON(response["species"])}),
                    resultsElement
                );
            }
        });

    }

};
