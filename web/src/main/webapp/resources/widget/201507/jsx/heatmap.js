/** @jsx React.DOM */

/*global React */
/* Modules and parameters for their init methods are passed in here.
 Parameters that affect how the DOM is generated as passed in as props. */

/* State stores: Levels/Quartiles displayed and checked columns
 */

var heatmapModule = (function($, React, genePropertiesTooltipModule, factorTooltipModule, contrastTooltipModule, helpTooltipsModule, baselineVarianceModule, EventEmitter, Modernizr) {

    var TypeEnum = {
        BASELINE: { isBaseline: true, heatmapTooltip: '#heatMapTableCellInfo', legendTooltip: '#gradient-base' },
        PROTEOMICS_BASELINE: { isBaseline: true, isProteomics: true, heatmapTooltip: '#heatMapTableCellInfo-proteomics', legendTooltip: '#gradient-base' },
        DIFFERENTIAL: { isDifferential: true, heatmapTooltip: '#heatMapTableCellInfo-differential' },
        MULTIEXPERIMENT: { isMultiExperiment: true, heatmapTooltip: '#heatMapTableCellInfo-multiexp', legendTooltip: '#gradient-base-crossexp' }
    };

    var build = function build(type, heatmapConfig, eventEmitter, $prefFormDisplayLevelsInputElement) {

        // ensemblSpecies is the first two words only, with underscores instead of spaces, and all lower case except for the first character
        // used to launch the ensembl genome browser for tracks
        var ensemblSpecies = (function toEnsemblSpecies(species) {
            function capitaliseFirstLetter(string)
            {
                return string.charAt(0).toUpperCase() + string.slice(1);
            }

            function firstTwoWords(text) {
                var words = text.split(" ");
                return (words.length <= 2) ? text : words[0] + " " + words[1];
            }

            return capitaliseFirstLetter(firstTwoWords(species).replace(" ", "_").toLowerCase());
        })(heatmapConfig.species);

        var ensemblHost = "http://" + ((heatmapConfig.ensemblDB == "ensembl") ? "www" : heatmapConfig.ensemblDB) + ".ensembl.org/";
        var grameneHost = "http://ensembl.gramene.org/";

        var Heatmap = React.createClass({displayName: 'Heatmap',

            getInitialState: function () {
                var displayLevels = $prefFormDisplayLevelsInputElement ? ($prefFormDisplayLevelsInputElement.val() === "true") : false;
                return {
                    showGeneSetProfiles: false,
                    displayLevels: displayLevels,
                    profiles: this.props.profiles,
                    selectedColumnId: null,
                    selectedGeneId: null,
                    selectedRadioButton: "gradients"};
            },

            selectColumn: function (columnId) {
                var selectedColumnId = (columnId === this.state.selectedColumnId) ? null : columnId;
                this.setState({selectedColumnId: selectedColumnId}, function() {
                    eventEmitter.emitEvent('onColumnSelectionChange', [selectedColumnId]);
                });
            },

            selectGene: function (geneId) {
                var selectedGeneId = (geneId === this.state.selectedGeneId) ? null : geneId;
                this.setState({selectedGeneId: selectedGeneId}, function() {
                    eventEmitter.emitEvent('onGeneSelectionChange', [selectedGeneId]);
                });
            },

            toggleGeneSets: function () {
                var newProfiles = this.state.showGeneSetProfiles ? this.props.profiles : this.props.geneSetProfiles;
                this.setState({showGeneSetProfiles: !this.state.showGeneSetProfiles, profiles: newProfiles});
            },

            toggleDisplayLevels: function () {
                var newDisplayLevels = !this.state.displayLevels;
                this.setState({displayLevels: newDisplayLevels});
                if ($prefFormDisplayLevelsInputElement) {
                    $prefFormDisplayLevelsInputElement.val(newDisplayLevels);
                }
            },

            toggleRadioButton: function(newSelected) {
                this.setState({selectedRadioButton: newSelected});
                this.setState({displayLevels: (newSelected === "levels")}); //update the LegendType
            },

            isMicroarray: function () {
                return !(typeof(this.props.profiles.rows[0].designElement) === "undefined");
            },

            hasQuartiles: function() {
                var hasQuartiles = false;
                for(var i=0; i < this.props.profiles.rows[0].expressions.length; i++) {
                    if(this.props.profiles.rows[0].expressions[i].quartiles != undefined) {
                        hasQuartiles = true;
                        break;
                    }
                }
                return hasQuartiles;
            },

            isSingleGeneResult: function () {
                return (this.props.profiles.rows.length == 1);
            },

            componentDidMount: function() {
                // Default settings
                var settings = {
                    scrollThrottle: 10,
                    resizeThrottle: 250
                };

                var $w	            = $(window),
                    $t	            = $(this.refs.heatmapTable.getDOMNode()),
                    $stickyInsct    = $(this.refs.stickyIntersect.getDOMNode()),
                    $stickyCol      = $(this.refs.stickyColumn.getDOMNode()),
                    $stickyHead     = $(this.refs.stickyHeader.getDOMNode()),
                    $stickyWrap     = $(this.refs.stickyWrap.getDOMNode()),
                    $countAndLegend = $(this.refs.countAndLegend.getDOMNode());

                if($t.hasClass('overflow-y')) $t.removeClass('overflow-y').parent().addClass('overflow-y');

                // Set widths
                var setWidths = function () {
                        $t
                            .find('thead th').each(function (i) {
                                $stickyHead.find('th').eq(i).width($(this).width());
                            })
                            .end()
                            .find('tr').each(function (i) {
                                $stickyCol.find('tr').eq(i).height($(this).height());
                            });

                        // Set width of sticky header table and intersect. WebKit does it wrong...
                        if ($.browser.webkit) {
                            $stickyHead
                                .width($stickyWrap.width())
                                .find('table')
                                .width($t.outerWidth());
                            $stickyInsct.find('table').width($t.find('thead th').eq(0).outerWidth() + 1);
                            $stickyCol.find('table').width($t.find('thead th').eq(0).outerWidth() + 1);
                        } else {
                            $stickyHead
                                .width($stickyWrap.width())
                                .find('table')
                                .width($t.width());
                            $stickyInsct.find('table').width($t.find('thead th').eq(0).outerWidth());
                            $stickyCol.find('table').width($t.find('thead th').eq(0).outerWidth());
                        }

                        // Set width of sticky table col
                        $stickyInsct.find('tr:nth-child(2) th').each(function(i) {
                            $(this).width($t.find('tr:nth-child(2) th').eq(i).width());
                        });

                        // Set position sticky col
                        $stickyHead.add($stickyInsct).add($stickyCol).css({
                            left: $stickyWrap.offset().left,
                            top: $stickyWrap.offset().top,
                        });
                    },
                    repositionSticky = function () {
                        // Return value of calculated allowance
                        var allowance = calcAllowance();

                        $stickyHead.find('table').css({
                            left: -$stickyWrap.scrollLeft()
                        });
                        $stickyCol.css({
                            top: $stickyWrap.offset().top - $w.scrollTop(),
                            left: $stickyWrap.offset().left
                        });

                        // 1. Position sticky header based on viewport scrollTop
                        if ($w.scrollTop() + $countAndLegend.outerHeight() > $t.offset().top &&
                            $w.scrollTop() + $countAndLegend.outerHeight() < $t.offset().top + $t.outerHeight() - allowance) {
                            // When top of viewport is in the table itself
                            $stickyHead.add($stickyInsct).css({
                                opacity: 1,
                                top: $countAndLegend.outerHeight()
                            });
                        } else if ($w.scrollTop() + $countAndLegend.outerHeight() > $t.offset().top + $t.outerHeight() - allowance) {
                            $stickyHead.add($stickyInsct).css({
                                opacity: 1,
                                top: $t.offset().top + $t.outerHeight() - allowance - $w.scrollTop()
                            });
                        } else {
                            // When top of viewport is above or below table
                            $stickyHead.add($stickyInsct).css({
                                opacity: 0,
                                top: $stickyWrap.offset().top - $w.scrollTop()
                            });
                        }

                        // 2. Now deal with positioning of sticky column
                        if($stickyWrap.scrollLeft() > 0) {
                            // When left of wrapping parent is out of view
                            $stickyCol.css({
                                opacity: 1,
                                "z-index": 40
                            });
                        } else {
                            $stickyCol.css({
                                opacity: 0,
                                "z-index": -5
                            });
                        }
                    },
                    calcAllowance = function () {
                        var rowHeight = 0;
                        // Calculate allowance
                        $t.find('tbody tr:lt(1)').each(function () {
                            rowHeight += $(this).height();
                        });
                        return rowHeight + $stickyHead.height();
                    };

                $t.parent('.gxaStickyTableWrap').scroll(repositionSticky);
                $w
                    .load(setWidths)
                    .resize($.debounce(settings.resizeThrottle, function () {
                        setWidths();
                        repositionSticky();
                    }))
                    .scroll(repositionSticky);

                $(this.refs.countAndLegend.getDOMNode()).hcSticky({bottomEnd: calcAllowance()});
                $w.resize();
            },

            legendType: function () {
                return (type.isBaseline || type.isMultiExperiment ?
                    LegendBaseline({displayLevels: this.state.displayLevels, minExpressionLevel: this.state.profiles.minExpressionLevel, maxExpressionLevel: this.state.profiles.maxExpressionLevel}) :
                    LegendDifferential({displayLevels: this.state.displayLevels, minDownLevel: this.state.profiles.minDownLevel, maxDownLevel: this.state.profiles.maxDownLevel, minUpLevel: this.state.profiles.minUpLevel, maxUpLevel: this.state.profiles.maxUpLevel}));
            },

            render: function () {
                var paddingMargin = "15px";

                // TODO id="heatmap-table" used to highlight the anatomogram in anatomogramModule.js ; this will need to change for the faceted search with multiple anatomograms
                return (
                    React.DOM.div(null, 
                        React.DOM.div({ref: "countAndLegend", className: "gxaHeatmapCountAndLegend", style: {"padding-bottom": paddingMargin}}, 
                            React.DOM.div({style: {display: "inline-block", 'vertical-align': "top"}}, 
                                type.isMultiExperiment ? React.DOM.span({id: "geneCount"}, "Showing ", this.state.profiles.rows.length, " of ", this.state.profiles.searchResultTotal, " experiments found: ") :
                                                          React.DOM.span({id: "geneCount"}, "Showing ", this.state.profiles.rows.length, " of ", this.state.profiles.searchResultTotal, " ", this.state.showGeneSetProfiles ? 'gene sets' : 'genes', " found: "), 
                                this.props.geneSetProfiles && !type.isMultiExperiment ? React.DOM.a({href: "javascript:void(0)", onClick: this.toggleGeneSets}, this.state.showGeneSetProfiles ? '(show individual genes)' : '(show by gene set)') : ''
                            ), 
                            React.DOM.div({style: {display: "inline-block", "padding-left": "10px", "vertical-align": "top"}}, 
                                DownloadProfilesButton({ref: "downloadProfilesButton"})
                            ), 
                            this.legendType()
                        ), 

                        React.DOM.div({ref: "stickyWrap", className: "gxaStickyTableWrap", style: {"margin-top": paddingMargin}}, 
                            React.DOM.table({ref: "heatmapTable", className: "gxaTableGrid gxaStickyEnabled", id: "heatmap-table"}, 
                                HeatmapTableHeader({ref: "heatmapTableHeader", isMicroarray: this.isMicroarray(), hasQuartiles: this.hasQuartiles(), isSingleGeneResult: this.isSingleGeneResult(), columnHeaders: this.props.columnHeaders, selectedColumnId: this.state.selectedColumnId, selectColumn: this.selectColumn, displayLevels: this.state.displayLevels, toggleDisplayLevels: this.toggleDisplayLevels, showGeneSetProfiles: this.state.showGeneSetProfiles, selectedRadioButton: this.state.selectedRadioButton, toggleRadioButton: this.toggleRadioButton, renderContrastFactorHeaders: true}), 
                                HeatmapTableRows({profiles: this.state.profiles.rows, selectedGeneId: this.state.selectedGeneId, selectGene: this.selectGene, displayLevels: this.state.displayLevels, showGeneSetProfiles: this.state.showGeneSetProfiles, selectedRadioButton: this.state.selectedRadioButton, hasQuartiles: this.hasQuartiles(), isSingleGeneResult: this.isSingleGeneResult(), renderExpressionCells: true})
                            ), 
                            React.DOM.div({ref: "stickyIntersect", className: "gxaStickyTableIntersect"}, 
                                React.DOM.table(null, 
                                    HeatmapTableHeader({isMicroarray: this.isMicroarray(), hasQuartiles: this.hasQuartiles(), isSingleGeneResult: this.isSingleGeneResult(), columnHeaders: this.props.columnHeaders, selectedColumnId: this.state.selectedColumnId, selectColumn: this.selectColumn, displayLevels: this.state.displayLevels, toggleDisplayLevels: this.toggleDisplayLevels, showGeneSetProfiles: this.state.showGeneSetProfiles, selectedRadioButton: this.state.selectedRadioButton, toggleRadioButton: this.toggleRadioButton, renderContrastFactorHeaders: false})
                                )
                            ), 
                            React.DOM.div({ref: "stickyColumn", className: "gxaStickyTableColumn"}, 
                                React.DOM.table(null, 
                                    HeatmapTableHeader({isMicroarray: this.isMicroarray(), hasQuartiles: this.hasQuartiles(), isSingleGeneResult: this.isSingleGeneResult(), columnHeaders: this.props.columnHeaders, selectedColumnId: this.state.selectedColumnId, selectColumn: this.selectColumn, displayLevels: this.state.displayLevels, toggleDisplayLevels: this.toggleDisplayLevels, showGeneSetProfiles: this.state.showGeneSetProfiles, selectedRadioButton: this.state.selectedRadioButton, toggleRadioButton: this.toggleRadioButton, renderContrastFactorHeaders: false}), 
                                    HeatmapTableRows({profiles: this.state.profiles.rows, selectedGeneId: this.state.selectedGeneId, selectGene: this.selectGene, displayLevels: this.state.displayLevels, showGeneSetProfiles: this.state.showGeneSetProfiles, selectedRadioButton: this.state.selectedRadioButton, hasQuartiles: this.hasQuartiles(), isSingleGeneResult: this.isSingleGeneResult(), renderExpressionCells: false})
                                )
                            ), 
                            React.DOM.div({ref: "stickyHeader", className: "gxaStickyTableHeader"}, 
                                React.DOM.table(null, 
                                    HeatmapTableHeader({isMicroarray: this.isMicroarray(), hasQuartiles: this.hasQuartiles(), isSingleGeneResult: this.isSingleGeneResult(), columnHeaders: this.props.columnHeaders, selectedColumnId: this.state.selectedColumnId, selectColumn: this.selectColumn, displayLevels: this.state.displayLevels, toggleDisplayLevels: this.toggleDisplayLevels, showGeneSetProfiles: this.state.showGeneSetProfiles, selectedRadioButton: this.state.selectedRadioButton, toggleRadioButton: this.toggleRadioButton, renderContrastFactorHeaders: true})
                                )
                            )
                        )

                    )
                );
            }
        });

        var DownloadProfilesButton = (function (contextRoot, downloadProfilesURL) {
            return React.createClass({
                render: function () {
                    return (
                        React.DOM.a({id: "download-profiles-link", ref: "downloadProfilesLink", 
                           title: "Download all results.", 
                           href: contextRoot + downloadProfilesURL, className: "gxaButtonImage", target: "_blank"}, 
                           React.DOM.img({id: "download-profiles", alt: "Download query results", style: {width: "20px"}, 
                                src: contextRoot + "/resources/images/download_blue_small.png"})
                        )
                    );
                },

                componentDidMount: function () {
                    var downloadProfilesLink = this.refs.downloadProfilesLink.getDOMNode();

                    $(downloadProfilesLink).tooltip();

                    $(document).ready(function () {
                        // call this inside ready() otherwise IE8 will be stuck with a "1 item remaining" message
                        $(downloadProfilesLink).button();
                    });
                }
            });
        })(heatmapConfig.contextRoot, heatmapConfig.downloadProfilesURL);

        var LegendBaseline = (function (contextRoot, formatBaselineExpression) {
            return React.createClass({
                render: function () {
                    return (
                        React.DOM.div({style: {display: "inline-block", "padding-left": "20px"}, className: "gxaHeatmapLegendGradient"}, 
                            React.DOM.div({style: {display: "inline-table"}}, 
                                LegendRow({displayLevels: this.props.displayLevels, lowExpressionLevel: formatBaselineExpression(this.props.minExpressionLevel), highExpressionLevel: formatBaselineExpression(this.props.maxExpressionLevel), lowValueColour: "#C0C0C0", highValueColour: "#0000FF"})
                            ), 
                            React.DOM.div({ref: "legendHelp", 'data-help-loc': type.legendTooltip, style: {display: "inline-block", "vertical-align": "top", "padding-left": "2px"}})
                        )
                        );
                },

                componentDidMount: function () {
                    helpTooltipsModule.init('experiment', contextRoot, this.refs.legendHelp.getDOMNode());
                }
            });
        })(heatmapConfig.contextRoot, formatBaselineExpression);


        var LegendDifferential = (function (contextRoot) {
            return React.createClass({
                render: function () {
                    return (
                        React.DOM.div({style: {display: "inline-block", "padding-left": "20px"}, className: "gxaHeatmapLegendGradient"}, 
                            React.DOM.div({style: {display: "inline-table"}}, 
                                !isNaN(this.props.minDownLevel) && !isNaN(this.props.maxDownLevel) ? LegendRow({displayLevels: this.props.displayLevels, lowExpressionLevel: this.props.minDownLevel, highExpressionLevel: this.props.maxDownLevel, lowValueColour: "#C0C0C0", highValueColour: "#0000FF"}) : null, 
                                !isNaN(this.props.minUpLevel) && !isNaN(this.props.maxUpLevel) ? LegendRow({displayLevels: this.props.displayLevels, lowExpressionLevel: this.props.minUpLevel, highExpressionLevel: this.props.maxUpLevel, lowValueColour: "#FFAFAF", highValueColour: "#FF0000"}) : null
                            ), 
                            React.DOM.div({ref: "legendHelp", 'data-help-loc': "#gradient-differential", style: {display: "inline-block", 'vertical-align': "top", "padding-left": "2px"}})
                        )
                    );
                },

                componentDidMount: function () {
                    helpTooltipsModule.init('experiment', contextRoot, this.refs.legendHelp.getDOMNode());
                }
            });
        })(heatmapConfig.contextRoot);


        var LegendRow = React.createClass({displayName: 'LegendRow',
            render: function () {
                var BACKGROUND_IMAGE_TEMPLATE = "-webkit-gradient(linear, left top, right top,color-stop(0, ${lowValueColour}), color-stop(1, ${highValueColour}));background-image: -moz-linear-gradient(left, ${lowValueColour}, ${highValueColour});background-image: -ms-linear-gradient(left, ${lowValueColour}, ${highValueColour}); background-image: -o-linear-gradient(left, ${lowValueColour}, ${highValueColour})";
                var backgroundImage = BACKGROUND_IMAGE_TEMPLATE.replace(/\${lowValueColour}/g, this.props.lowValueColour).replace(/\${highValueColour}/g, this.props.highValueColour);

                // for IE8 and 9
                var LT_IE10_FILTER_TEMPLATE = "progid:DXImageTransform.Microsoft.Gradient(GradientType =1,startColorstr=${lowValueColour},endColorstr=${highValueColour})";
                var lt_ie10_filter = LT_IE10_FILTER_TEMPLATE.replace(/\${lowValueColour}/, this.props.lowValueColour).replace(/\${highValueColour}/, this.props.highValueColour);

                return (
                    React.DOM.div({style: {display: "table-row"}}, 
                        React.DOM.div({style: this.props.displayLevels ? {'white-space': "nowrap", "font-size": "10px", 'vertical-align': "middle", display: "table-cell"} : {'white-space': "nowrap", "font-size": "10px", 'vertical-align': "middle", display: "table-cell", visibility: "hidden"}, className: "gxaGradientLevelMin"}, this.props.lowExpressionLevel), 
                        React.DOM.div({style: {display: "table-cell"}}, 
                            React.DOM.span({className: "color-gradient", style: {overflow: "auto", 'vertical-align': "middle", "background-image": backgroundImage, filter: lt_ie10_filter, width: "200px", height: "15px", margin: "2px 6px 2px 6px", display: "inline-block"}})
                        ), 
                        React.DOM.div({style: this.props.displayLevels ? {'white-space': "nowrap", "font-size": "10px", 'vertical-align': "middle", display: "table-cell"} : {'white-space': "nowrap", "font-size": "10px", 'vertical-align': "middle", display: "none", visibility: "hidden"}, className: "gxaGradientLevelMax"}, this.props.highExpressionLevel)
                    )
                );
            }
        });

        var HeatmapTableHeader = React.createClass({displayName: 'HeatmapTableHeader',

            renderContrastFactorHeaders: function () {
                if (type.isBaseline) {
                    return (FactorHeaders({assayGroupFactors: this.props.columnHeaders, selectedColumnId: this.props.selectedColumnId, selectColumn: this.props.selectColumn, experimentAccession: heatmapConfig.experimentAccession}) );
                }
                else if (type.isDifferential) {
                    return (ContrastHeaders({contrasts: this.props.columnHeaders, selectedColumnId: this.props.selectedColumnId, selectColumn: this.props.selectColumn, experimentAccession: heatmapConfig.experimentAccession, showMaPlotButton: heatmapConfig.showMaPlotButton, gseaPlots: heatmapConfig.gseaPlots}));
                }
                else if (type.isMultiExperiment) {
                     return (FactorHeaders({assayGroupFactors: this.props.columnHeaders, selectedColumnId: this.props.selectedColumnId, selectColumn: this.props.selectColumn}));
                }
            },

            render: function () {
                var showGeneProfile = this.props.showGeneSetProfiles ? 'Gene set' : 'Gene';
                var showExperimentProfile = type.isMultiExperiment ? 'Experiment' : showGeneProfile;

                return (
                    React.DOM.thead(null, 
                        React.DOM.tr(null, 
                            React.DOM.th({className: "gxaHorizontalHeaderCell gxaHeatmapTableIntersect", colSpan: this.props.isMicroarray ? 2 : undefined}, 
                                TopLeftCorner({hasQuartiles: this.props.hasQuartiles, isSingleGeneResult: this.props.isSingleGeneResult, displayLevels: this.props.displayLevels, toggleDisplayLevels: this.props.toggleDisplayLevels, selectedRadioButton: this.props.selectedRadioButton, toggleRadioButton: this.props.toggleRadioButton})
                            ), 

                             this.props.renderContrastFactorHeaders ? this.renderContrastFactorHeaders() : null
                        ), 

                        React.DOM.tr(null, 
                            React.DOM.th({className: "gxaHorizontalHeaderCell gxaHeatmapTableIntersect", style:  this.props.isMicroarray ? {width:"166px"} : undefined}, React.DOM.div(null, showExperimentProfile )), 
                             this.props.isMicroarray ? React.DOM.th({className: "gxaHorizontalHeaderCell gxaHeatmapTableIntersect"}, React.DOM.div(null, "Design Element")) : null
                        )
                    )
                );
            }

        });

        function restrictLabelSize(label, maxSize) {
            var result = label;
            if (result.length > maxSize + 1) {  // +1 to account for the extra ellipsis character appended
                result = result.substring(0, maxSize);
                if (result.lastIndexOf(" ") > maxSize - 5) {
                    result = result.substring(0, result.lastIndexOf(" "));
                }
                result = result + "…";
            }
            return result;
        }

        var FactorHeaders = React.createClass({displayName: 'FactorHeaders',

            render: function () {
                var factorHeaders = this.props.assayGroupFactors.map(function (assayGroupFactor) {
                    return FactorHeader({factorName: assayGroupFactor.factorValue, svgPathId: assayGroupFactor.factorValueOntologyTermId, assayGroupId: assayGroupFactor.assayGroupId, experimentAccession: this.props.experimentAccession, 
                            selectColumn: this.props.selectColumn, selected: assayGroupFactor.assayGroupId === this.props.selectedColumnId});
                }.bind(this));

                return (
                    React.DOM.div(null, factorHeaders)
                );
            }

        });

        var FactorHeader = (function (contextRoot, accessKey, enableEnsemblLauncher, csstransforms) {
            return React.createClass({

                getInitialState: function () {
                    return ({hover:false, selected:false});
                },

                onMouseEnter: function () {
                    this.setState({hover:true});
                },

                onMouseLeave: function () {
                    this.setState({hover:false});
                },

                onClick: function () {
                    this.props.selectColumn(this.props.assayGroupId);
                },

                componentDidMount: function () {
                    if(!type.isMultiExperiment) {
                        factorTooltipModule.init(contextRoot, accessKey, this.getDOMNode());
                    }
                },

                render: function () {

                    var showSelectTextOnHover = this.state.hover && !this.props.selected ? React.DOM.span({style: {position: "absolute", width:"10px", right:"0px", left:"95px", float:"right", color:"green"}}, "  select") : null;
                    var showTickWhenSelected = this.props.selected ? React.DOM.span({className: "rotate_tick", style: {position: "absolute", width:"5px", right:"0px", left:"125px", float:"right", color:"green"}}, " ✔ "): null ;
                    var thClass = "rotated_cell gxaHoverableHeader" + (this.props.selected ? " gxaVerticalHeaderCell-selected" : " gxaVerticalHeaderCell") + (enableEnsemblLauncher ? " gxaSelectableHeader" : "");
                    var divClass = "rotate_text factor-header";
                    var factorName = csstransforms ? restrictLabelSize(this.props.factorName, 14) : this.props.factorName;

                    return (
                        React.DOM.th({className: thClass, onMouseEnter: enableEnsemblLauncher ? this.onMouseEnter : undefined, onMouseLeave: enableEnsemblLauncher ? this.onMouseLeave : undefined, onClick: enableEnsemblLauncher ? this.onClick : undefined, rowSpan: "2"}, 
                            React.DOM.div({'data-svg-path-id': this.props.svgPathId, 'data-assay-group-id': this.props.assayGroupId, 'data-experiment-accession': this.props.experimentAccession, className: divClass}, 
                                factorName, 
                                showSelectTextOnHover, 
                                showTickWhenSelected
                            )
                        )
                    );
                }
            });
        })(heatmapConfig.contextRoot, heatmapConfig.accessKey, heatmapConfig.enableEnsemblLauncher, Modernizr.csstransforms);

        var ContrastHeaders = React.createClass({displayName: 'ContrastHeaders',

            render: function () {
                var contrastHeaders = this.props.contrasts.map(function (contrast) {
                    var gseaPlotsThisContrast = this.props.gseaPlots ? this.props.gseaPlots[contrast.id] : {go: false, interpro: false, reactome: false};
                    return ContrastHeader({selectColumn: this.props.selectColumn, selected: contrast.id === this.props.selectedColumnId, contrastName: contrast.displayName, arrayDesignAccession: contrast.arrayDesignAccession, contrastId: contrast.id, experimentAccession: this.props.experimentAccession, showMaPlotButton: this.props.showMaPlotButton, 
                    showGseaGoPlot: gseaPlotsThisContrast.go, 
                    showGseaInterproPlot: gseaPlotsThisContrast.interpro, 
                    showGseaReactomePlot: gseaPlotsThisContrast.reactome});
                }.bind(this));

                return (
                    React.DOM.div(null, contrastHeaders)
                );
            }

        });

        var ContrastHeader = (function (contextRoot, accessKey, enableEnsemblLauncher, csstransforms) {
            return React.createClass({

                getInitialState: function () {
                    return ({hover:false, selected:false});
                },

                onMouseEnter: function () {
                    this.setState({hover:true});
                },

                onMouseLeave: function () {
                    this.setState({hover:false});
                },

                onClick: function () {
                    this.props.selectColumn(this.props.contrastId);
                },

                componentDidMount: function () {
                    contrastTooltipModule.init(contextRoot, accessKey, this.getDOMNode());

                    if (this.showPlotsButton()) {
                        this.renderToolBarContent(this.refs.plotsToolBarContent.getDOMNode());

                        var plotsButton = this.refs.plotsButton.getDOMNode();
                        $(plotsButton).tooltip().button();
                        $(plotsButton).toolbar({
                            content: this.refs.plotsToolBarContent.getDOMNode(),
                            position: 'right'
                        });

                    }
                },

                renderToolBarContent: function(contentNode) {

                    var $contentNode = $(contentNode);

                    var maPlotURL = contextRoot + '/external-resources/' + this.props.experimentAccession + '/' + (this.props.arrayDesignAccession ? this.props.arrayDesignAccession + '/' : '' ) + this.props.contrastId + '/ma-plot.png';
                    var gseaGoPlotUrl = contextRoot + '/external-resources/' + this.props.experimentAccession + '/' + this.props.contrastId + '/gsea_go.png';
                    var gseaInterproPlotUrl = contextRoot + '/external-resources/' + this.props.experimentAccession + '/' + this.props.contrastId + '/gsea_interpro.png';
                    var gseaReactomePlotUrl = contextRoot + '/external-resources/' + this.props.experimentAccession + '/' + this.props.contrastId + '/gsea_reactome.png';

                    var content =
                        React.DOM.div(null, 
                            this.props.showMaPlotButton ? React.DOM.a({href: maPlotURL, id: "maButtonID", title: "Click to view MA plot for the contrast across all genes", onClick: this.clickButton}, React.DOM.img({src: contextRoot + '/resources/images/maplot-button.png'})) : null, 
                            this.props.showGseaGoPlot ? React.DOM.a({href: gseaGoPlotUrl, id: "goButtonID", title: "Click to view GO terms enrichment analysis plot", onClick: this.clickButton}, React.DOM.img({src: contextRoot + '/resources/images/gsea-go-button.png'})) : null, 
                            this.props.showGseaInterproPlot ? React.DOM.a({href: gseaInterproPlotUrl, id: "interproButtonID", title: "Click to view Interpro domains enrichment analysis plot", onClick: this.clickButton}, React.DOM.img({src: contextRoot + '/resources/images/gsea-interpro-button.png'})) : null, 
                            this.props.showGseaReactomePlot ? React.DOM.a({href: gseaReactomePlotUrl, id: "reactomeButtonID", title: "Click to view Reactome pathways enrichment analysis plot", onClick: this.clickButton}, React.DOM.img({src: contextRoot + '/resources/images/gsea-reactome-button.png'})) : null
                        );

                    // the tool bar content will be copied around the DOM by the toolbar plugin
                    // so we render using static markup because otherwise when copied, we'll end up with
                    // duplicate data-reactids
                    $contentNode.html(React.renderComponentToStaticMarkup(content));

                    $contentNode.find('a').tooltip();

                    //need to use each here otherwise we get a fancybox error
                    $contentNode.find('a').each(function (index, button) {
                        $(button).fancybox({
                            padding:0,
                            openEffect:'elastic',
                            closeEffect:'elastic'
                        });
                    });
                },

                clickButton: function (event) {
                    // prevent contrast from being selected
                    event.stopPropagation();
                },

                showPlotsButton: function () {
                    return this.props.showMaPlotButton || this.props.showGseaGoPlot || this.props.showGseaInterproPlot || this.props.showGseaReactomePlot;
                },

                render: function () {
                    var thStyle = this.showPlotsButton() ? {"min-width": "80px"} : {};
                    var textStyle = this.showPlotsButton() ? {top: "57px"} : {};

                    var plotsButton = (
                        React.DOM.div({style: {"text-align":"right", "padding-right":"3px"}}, 
                            React.DOM.a({href: "#", ref: "plotsButton", onClick: this.clickButton, className: "gxaButtonImage", title: "Click to view plots"}, React.DOM.img({src: contextRoot + '/resources/images/yellow-chart-icon.png'}))
                        )
                    );

                    var showSelectTextOnHover = this.state.hover && !this.props.selected ? React.DOM.span({style: {position: "absolute", width:"10px", right:"0px", left:"95px", bottom:"-35px", color:"green"}}, "  select") : null;
                    var showTickWhenSelected = this.props.selected ? React.DOM.span({className: "rotate_tick", style: {position:"absolute", width:"5px", right:"0px", left:"125px", bottom:"-35px", color:"green"}}, " ✔ "): null;
                    var thClass = "rotated_cell gxaHoverableHeader" + (this.props.selected ? " gxaVerticalHeaderCell-selected" : " gxaVerticalHeaderCell") + (enableEnsemblLauncher ? " gxaSelectableHeader " : "");
                    var divClass = "rotate_text factor-header";
                    var contrastName = csstransforms ? restrictLabelSize(this.props.contrastName, 17) : this.props.contrastName;

                    return (
                        React.DOM.th({className: thClass, rowSpan: "2", style: thStyle, onMouseEnter: enableEnsemblLauncher ? this.onMouseEnter : undefined, onMouseLeave: enableEnsemblLauncher ? this.onMouseLeave : undefined, onClick: enableEnsemblLauncher ? this.onClick : undefined}, 
                            React.DOM.div({'data-contrast-id': this.props.contrastId, 'data-experiment-accession': this.props.experimentAccession, className: divClass, style: textStyle}, 
                                contrastName, 
                                showSelectTextOnHover, 
                                showTickWhenSelected
                            ), 
                                this.showPlotsButton() ? plotsButton : null, 
                                this.showPlotsButton() ? React.DOM.div({ref: "plotsToolBarContent", style: {display: "none"}}, "placeholder") : null
                        )
                    );
                }
            });
        })(heatmapConfig.contextRoot, heatmapConfig.accessKey, heatmapConfig.enableEnsemblLauncher, Modernizr.csstransforms);


        var EnsemblLauncher = (function (atlasHost, contextRoot, experimentAccession, accessKey, ensemblHost, ensemblSpecies, ensemblDB, columnType ) {

            var noSelectedColumnMessageArticle = (function (columnType) {
                var isVowel = (function() {
                    var re = /^[aeiou]$/i;
                    return function(char) {
                        return re.test(char);
                    }
                })();

                var beginsWithVowel = function (string) {
                    return isVowel(string.charAt(0));
                };

                return beginsWithVowel(columnType) ? "an " : "a ";
            })(columnType);

            return React.createClass({

                getInitialState: function () {
                    return {selectedColumnId: null, selectedGeneId: null, buttonText: ""};
                },

                componentDidMount: function () {
                    $(this.refs.ensemblButton.getDOMNode()).button({icons: {primary: "ui-icon-newwin"}});
                    if (heatmapConfig.ensemblDB == "plants") {
                        $(this.refs.grameneButton.getDOMNode()).button({icons: {primary: "ui-icon-newwin"}});
                    }
                    this.updateButton();
                    eventEmitter.addListener('onColumnSelectionChange', this.onColumnSelectionChange);
                    eventEmitter.addListener('onGeneSelectionChange', this.onGeneSelectionChange);
                },

                componentWillUnmount: function () {
                    eventEmitter.addListener('onColumnSelectionChange', this.onColumnSelectionChange);
                    eventEmitter.addListener('onGeneSelectionChange', this.onGeneSelectionChange);
                },

                onColumnSelectionChange: function (selectedColumnId) {
                    this.setState({selectedColumnId: selectedColumnId});
                },

                onGeneSelectionChange: function (selectedGeneId) {
                    this.setState({selectedGeneId: selectedGeneId});
                },

                updateButton: function() {
                    var buttonEnabled = this.state.selectedColumnId && this.state.selectedGeneId ? true : false;
                    $(this.refs.ensemblButton.getDOMNode()).button("option", "disabled", !buttonEnabled);
                    if (heatmapConfig.ensemblDB == "plants") {
                        $(this.refs.grameneButton.getDOMNode()).button("option", "disabled", !buttonEnabled);
                    }
                },

                helpMessage: function (selectedColumnId, selectedGeneId) {
                    if (selectedColumnId && selectedGeneId) {
                        return "";
                    }

                    var noSelectedColumnMessage = selectedColumnId ? "" : columnType;
                    var noSelectedGeneMessage = selectedGeneId ? "" : "gene";

                    return "Please select " + noSelectedColumnMessageArticle + noSelectedColumnMessage + (!(selectedColumnId || selectedGeneId) ? " and a " : "") + noSelectedGeneMessage + " from the table";
                },

                componentDidUpdate: function () {
                    this.updateButton();
                },

                openEnsemblWindow: function (baseURL) {
                    if (!this.state.selectedColumnId || !this.state.selectedGeneId) {
                        return;
                    }
                    var trackFileHeader = experimentAccession + "." + this.state.selectedColumnId;
                    var atlasTrackBaseUrl = "http://" + atlasHost + contextRoot + "/experiments/" + experimentAccession + "/tracks/";
                    var contigviewbottom = "contigviewbottom=url:" + atlasTrackBaseUrl + trackFileHeader + (type.isBaseline ? ".genes.expressions.bedGraph" : ".genes.log2foldchange.bedGraph");
                    var tiling = (type.isBaseline || ensemblDB == "ensembl") ? "" : "=tiling,url:" + atlasTrackBaseUrl + trackFileHeader + ".genes.pval.bedGraph=pvalue;";
                    var url =  baseURL + ensemblSpecies + "/Location/View?g=" + this.state.selectedGeneId + ";db=core;" + contigviewbottom + tiling + ";format=BEDGRAPH";

                    window.open(
                        url,
                        '_blank'
                    );
                },

                render: function () {
                    return (
                        React.DOM.div({id: "ensembl-launcher-box", style: {width: "245px"}}, 
                            React.DOM.div({id: "ensembl-launcher-box-ensembl"}, 
                                React.DOM.label(null, "Ensembl Genome Browser"), 
                                React.DOM.img({src: "/gxa/resources/images/ensembl.gif", style: {padding: "0px 5px"}}), 
                                React.DOM.button({ref: "ensemblButton", onClick: this.openEnsemblWindow.bind(this, ensemblHost)}, "Open")
                            ), 
                             heatmapConfig.ensemblDB == "plants" ?
                                React.DOM.div({id: "ensembl-launcher-box-gramene"}, 
                                    React.DOM.label(null, "Gramene Genome Browser"), 
                                    React.DOM.img({src: "/gxa/resources/images/gramene.png", style: {padding: "0px 5px"}}), 
                                    React.DOM.button({ref: "grameneButton", onClick: this.openEnsemblWindow.bind(this, grameneHost)}, "Open")
                                )
                                : null, 
                            
                            React.DOM.div({style: {"font-size": "x-small", height: "30px", padding: "9px 9px"}}, this.helpMessage(this.state.selectedColumnId, this.state.selectedGeneId))
                        )
                        );
                }
            });
        })(heatmapConfig.atlasHost, heatmapConfig.contextRoot, heatmapConfig.experimentAccession, heatmapConfig.accessKey, ensemblHost, ensemblSpecies, heatmapConfig.ensemblDB, heatmapConfig.columnType);


        var TopLeftCorner = (function (contextRoot) {
            return React.createClass({

                displayLevelsBaseline: function() {
                    var displayLevelsButton = type.isDifferential ? DisplayLevelsButtonDifferential : DisplayLevelsButtonBaseline;
                    return ( this.props.hasQuartiles && this.props.isSingleGeneResult ?
                        LevelsRadioGroup({selectedRadioButton: this.props.selectedRadioButton, toggleRadioButton: this.props.toggleRadioButton}) :
                        displayLevelsButton({displayLevels: this.props.displayLevels, toggleDisplayLevels: this.props.toggleDisplayLevels})
                    );
                },

                render: function () {
                    return (
                        React.DOM.div({className: "gxaHeatmapMatrixTopLeftCorner"}, 
                            React.DOM.span({id: "tooltip-span", 'data-help-loc': type.heatmapTooltip, ref: "tooltipSpan"}), 
                            this.displayLevelsBaseline()
                        )
                    );
                },

                componentDidMount: function () {
                    helpTooltipsModule.init('experiment', contextRoot, this.refs.tooltipSpan.getDOMNode());
                }
            });
        })(heatmapConfig.contextRoot);

        var createDisplayLevelsButton = function (hideText, showText) {

            return React.createClass({

                buttonText: function (displayLevels) {
                    return displayLevels ? hideText : showText;
                },

                updateButtonText: function () {
                    $(this.getDOMNode()).button({ label: this.buttonText(this.props.displayLevels) });
                },

                render: function () {
                    return (
                        React.DOM.button({id: "display-levels", onClick: this.props.toggleDisplayLevels})
                    );
                },

                componentDidMount: function () {
                    this.updateButtonText();
                },

                componentDidUpdate: function () {
                    this.updateButtonText();
                }

            });
        };

        var LevelsRadioGroup = React.createClass({displayName: 'LevelsRadioGroup',
            getInitialState: function() {
                return {value: this.props.selectedRadioButton};
            },

            render: function() {
                return (
                    RadioGroup({name: "displayLevelsGroup", value: this.props.selectedRadioButton, onChange: this.handleChange}, 
                        React.DOM.div({style: {"margin-left": "10px", "margin-top": "8px"}}, 
                            React.DOM.input({type: "radio", value: "gradients"}), "Display gradients", React.DOM.br(null), 
                            React.DOM.input({type: "radio", value: "levels"}), "Display levels", React.DOM.br(null), 
                            React.DOM.input({type: "radio", value: "variance"}), "Display variance"
                        )
                    )
                );
            },

            handleChange: function(event) {
                this.props.toggleRadioButton(event.target.value);
                this.setState({value: this.props.selectedRadioButton});
            }
        });


        var DisplayLevelsButtonBaseline = createDisplayLevelsButton('Hide levels', 'Display levels');

        var DisplayLevelsButtonDifferential = createDisplayLevelsButton('Hide log<sub>2</sub>-fold change', 'Display log<sub>2</sub>-fold change');

        var HeatmapTableRows = React.createClass({displayName: 'HeatmapTableRows',

            profileRowType: function (profile)  {
                return (type.isMultiExperiment ?
                    GeneProfileRow({id: profile.id, name: profile.name, experimentType: profile.experimentType, expressions: profile.expressions, serializedFilterFactors: profile.serializedFilterFactors, displayLevels: this.props.displayLevels, renderExpressionCells: this.props.renderExpressionCells})
                    :
                    GeneProfileRow({selected: profile.id === this.props.selectedGeneId, selectGene: this.props.selectGene, designElement: profile.designElement, id: profile.id, name: profile.name, expressions: profile.expressions, displayLevels: this.props.displayLevels, showGeneSetProfiles: this.props.showGeneSetProfiles, selectedRadioButton: this.props.selectedRadioButton, hasQuartiles: this.props.hasQuartiles, isSingleGeneResult: this.props.isSingleGeneResult, renderExpressionCells: this.props.renderExpressionCells})
                );
            },

            render: function () {
                var geneProfilesRows = this.props.profiles.map(function (profile) {
                    return this.profileRowType(profile);
                }.bind(this));

                return (
                    React.DOM.tbody(null, 
                        geneProfilesRows
                    )
                );
            }
        });

        var GeneProfileRow = (function (contextRoot, toolTipHighlightedWords, isExactMatch, enableGeneLinks, enableEnsemblLauncher, geneQuery) {

            return React.createClass({

                getInitialState: function () {
                    return ({hover:false, selected:false, levels: this.props.displayLevels});
                },

                onMouseEnter: function () {
                    this.setState({hover:true});
                },

                onMouseLeave: function () {
                    this.setState({hover:false});
                },

                onClick: function () {
                    this.props.selectGene(this.props.id);
                },

                geneNameLinked: function () {
                    var experimentURL = '/experiments/' + this.props.id + '?geneQuery=' + geneQuery + (this.props.serializedFilterFactors ? "&serializedFilterFactors=" + encodeURIComponent(this.props.serializedFilterFactors) : "");
                    var geneURL = this.props.showGeneSetProfiles ? '/query?geneQuery=' + this.props.name + '&exactMatch=' + isExactMatch : '/genes/' + this.props.id;

                    var titleTooltip = type.isMultiExperiment ? (this.props.experimentType == "PROTEOMICS_BASELINE" ? "Protein Expression" : "RNA Expression" ) : "";

                    var url = (type.isMultiExperiment ? experimentURL : geneURL);

                    // don't render id for gene sets to prevent tooltips
                    // The vertical align in the <a> element is needed because the kerning in the font used in icon-conceptual is vertically off
                    return (
                        React.DOM.span({title: titleTooltip, style: {"float": "left"}}, 
                            React.DOM.span({className: "icon icon-conceptual icon-c2", 'data-icon': type.isMultiExperiment ? (this.props.experimentType == "PROTEOMICS_BASELINE" ? 'P' : 'd') : ''}), 
                            React.DOM.a({ref: "geneName", title: "", id: this.props.showGeneSetProfiles ? '' : this.props.id, href: contextRoot + url, onClick: this.geneNameLinkClicked, style: {"vertical-align": "15%"}}, this.props.name)
                        )
                    );
                },

                geneNameLinkClicked: function (event) {
                    // prevent row from being selected
                    event.stopPropagation();
                },

                geneNameNotLinked: function () {
                    // don't render id for gene sets to prevent tooltips
                    return (
                        React.DOM.span({style: {"float": "left"}, ref: "geneName", title: "", id: this.props.showGeneSetProfiles ? '' : this.props.id}, this.props.name)
                        );
                },

                displayLevelsRadio: function() {
                    if(this.props.hasQuartiles && this.props.isSingleGeneResult) {
                        return this.props.selectedRadioButton === "levels";
                    }
                    else return (this.props.displayLevels);
                },

                cellType: function (expression) {
                    if (type.isBaseline) {
                        if(this.props.selectedRadioButton === "variance") {
                            return (this.varianceExpressionCell(expression));
                        }
                        else {
                            return (
                                CellBaseline({factorName: expression.factorName, color: expression.color, value: expression.value, displayLevels: this.displayLevelsRadio(), svgPathId: expression.svgPathId, geneSetProfiles: this.props.showGeneSetProfiles, id: this.props.id, name: this.props.name})
                            );
                        }
                    }
                    else if (type.isDifferential) {
                        return (
                                CellDifferential({color: expression.color, foldChange: expression.foldChange, pValue: expression.pValue, tStat: expression.tStat, displayLevels: this.props.displayLevels, id: this.props.id, name: this.props.name})
                            );
                    }
                    else if (type.isMultiExperiment) {
                        return (
                                CellMultiExperiment({factorName: expression.factorName, serializedFilterFactors: this.props.serializedFilterFactors, color: expression.color, value: expression.value, displayLevels: this.props.displayLevels, svgPathId: expression.svgPathId, id: this.props.id, name: this.props.name})
                            );
                    }
                },

                cells: function (expressions) {
                    return expressions.map(function (expression) {
                        return this.cellType(expression);
                    }.bind(this));
                },

                varianceExpressionCell: function (expression) {
                    var CellBaselineVariance = baselineVarianceModule.build().CellBaselineVariance;
                    return (CellBaselineVariance({quartiles: expression.quartiles}) );

                },

                render: function () {
                    var showSelectTextOnHover = this.state.hover && !this.props.selected ? React.DOM.span({style: {"padding-top": "5px", "float": "right", "color": "green"}}, "select") : null;
                    var showTickWhenSelected = this.props.selected ? React.DOM.span({style: {"padding-top": "2px", "float": "right", "color": "green"}}, " ✔ "): null ;
                    var className = (this.props.selected ? "gxaHorizontalHeaderCell-selected gxaHoverableHeader" : "gxaHorizontalHeaderCell gxaHoverableHeader") + (enableEnsemblLauncher ? " gxaSelectableHeader" : "");
                    var rowClassName = type.isMultiExperiment ? (this.props.experimentType == "PROTEOMICS_BASELINE" ? "gxaProteomicsExperiment" : "gxaTranscriptomicsExperiment" ) : "";

                    return (
                        React.DOM.tr({className: rowClassName}, 
                            React.DOM.th({className: className, onMouseEnter: enableEnsemblLauncher ? this.onMouseEnter : undefined, onMouseLeave: enableEnsemblLauncher ? this.onMouseLeave : undefined, onClick: enableEnsemblLauncher ? this.onClick : undefined}, 
                                React.DOM.div(null, 
                                 enableGeneLinks ?  this.geneNameLinked() : this.geneNameNotLinked(), 
                                showSelectTextOnHover, 
                                showTickWhenSelected
                                )
                            ), 
                            this.props.designElement ? React.DOM.th({className: "gxaHeatmapTableDesignElement"}, this.props.designElement) : null, 
                            this.props.renderExpressionCells ? this.cells(this.props.expressions) : null
                        )
                    );
                },

                componentDidMount: function () {
                    if(!type.isMultiExperiment) {
                        genePropertiesTooltipModule.init(contextRoot, toolTipHighlightedWords, this.refs.geneName.getDOMNode());
                    }
                }
            });
        })(heatmapConfig.contextRoot, heatmapConfig.geneQuery, heatmapConfig.isExactMatch, heatmapConfig.enableGeneLinks, heatmapConfig.enableEnsemblLauncher, heatmapConfig.geneQuery);


        // expects number in the format #E# and displays exponent in superscript
        function formatScientificNotation(scientificNotationString) {

            var formatParts = scientificNotationString.split(/[Ee]/);

            if (formatParts.length == 1) {
                return (
                    React.DOM.span(null, scientificNotationString)
                );
            }

            var mantissa = formatParts[0];
            var exponent = formatParts[1];

            return (
                React.DOM.span(null, 
                    (mantissa !== "1") ? mantissa + " \u00D7 " : '', "10", React.DOM.span({style: {'vertical-align': 'super'}}, exponent)
                )
            );
        }

        function formatBaselineExpression(expressionLevel) {
            var numberExpressionLevel = +expressionLevel;
            return (numberExpressionLevel >= 100000 || numberExpressionLevel < 0.1) ? formatScientificNotation(numberExpressionLevel.toExponential(1).replace('+','')) : '' + numberExpressionLevel;
        }

        var CellBaseline = (function (contextRoot, experimentAccession, ensemblHost, ensemblSpecies, formatBaselineExpression) {

            function hasKnownExpression(value) {
                // true if not blank or UNKNOWN, ie: has a expression with a known value
                return (value && !isUnknownExpression(value));
            }

            function isUnknownExpression(value) {
                return (value === "UNKNOWN")
            }

            function noExpression(value) {
                return !value;
            }

            function unknownCell(geneSetProfiles) {
                return (
                    React.DOM.span({ref: "unknownCell", 'data-help-loc': geneSetProfiles ? '#heatMapTableGeneSetUnknownCell' : '#heatMapTableUnknownCell'})
                    );
            }

            return React.createClass({
                render: function () {
                    if (noExpression(this.props.value)) {
                        return (React.DOM.td(null));
                    }

                    var style = {"background-color": isUnknownExpression(this.props.value) ? "white" : this.props.color};

                    return (
                        React.DOM.td({style: style}, 
                            React.DOM.div({
                            className: "gxaHeatmapCell", 
                            style: {visibility: isUnknownExpression(this.props.value) || this.props.displayLevels ? "visible" : "hidden"}, 
                            'data-svg-path-id': this.props.svgPathId}, 
                                isUnknownExpression(this.props.value) ? unknownCell(this.props.geneSetProfiles) : formatBaselineExpression(this.props.value)
                            )
                        )
                        );
                },

                componentDidMount: function () {
                    this.addQuestionMarkTooltip();
                },

                // need this so that we re-add question mark tooltip, if it doesn't exist, when switching between
                // individual genes and gene sets
                componentDidUpdate: function () {
                    this.addQuestionMarkTooltip();
                },

                addQuestionMarkTooltip: function() {
                    function hasQuestionMark(unknownElement) {
                        return unknownElement.children.length;
                    }

                    if (isUnknownExpression(this.props.value) && !hasQuestionMark(this.refs.unknownCell.getDOMNode())) {
                        helpTooltipsModule.init('experiment', contextRoot, this.refs.unknownCell.getDOMNode());
                    }
                }

            });
        })(heatmapConfig.contextRoot, heatmapConfig.experimentAccession, ensemblHost, ensemblSpecies, formatBaselineExpression);


        var CellMultiExperiment = (function (contextRoot, ensemblHost, ensemblSpecies, geneId, geneName, formatBaselineExpression) {
            
            function isNAExpression(value) {
                return (value === "NT")
            }
            
            function noExpression(value) {
                return !value;
            }

            function tissueNotStudiedInExperiment() {
                return (
                    React.DOM.span(null, "NA")
                    );
            }

            return React.createClass({

                render: function () {

                    if (noExpression(this.props.value)) {
                        return (React.DOM.td(null));
                    }

                    var style = {"background-color": this.props.color};

                    return (
                        React.DOM.td({style: style}, 
                            React.DOM.div({
                            className: "gxaHeatmapCell", 
                            style: {visibility: isNAExpression(this.props.value) || this.props.displayLevels ? "visible" : "hidden"}, 
                            'data-svg-path-id': this.props.svgPathId}, 
                                isNAExpression(this.props.value) ? tissueNotStudiedInExperiment() : formatBaselineExpression(this.props.value)
                            )
                        )
                        );
                }
            });
        })(heatmapConfig.contextRoot, ensemblHost, ensemblSpecies, heatmapConfig.geneQuery, heatmapConfig.geneQuery, formatBaselineExpression);

        var CellDifferential = (function () {

            return React.createClass({

                hasValue: function () {
                    return (this.props.foldChange !== undefined);
                },

                render: function () {
                    if (!this.hasValue()) {
                        return (React.DOM.td(null));
                    }

                    return (
                        React.DOM.td({style: {"background-color": this.props.color}}, 
                            React.DOM.div({className: this.props.displayLevels ? "gxaShowCell" : "gxaHideCell"}, 
                                this.props.foldChange
                            )
                        )
                        );
                },

                componentDidMount: function () {
                    if (this.hasValue()) {
                        this.initTooltip(this.getDOMNode());
                    }
                },

                initTooltip: function(element) {

                    //TODO - build this from a React component, like we do for FactorTooltip
                    function buildHeatmapCellTooltip (pValue, tstatistic, foldChange) {

                        return "<table class='gxaTableGrid' style='margin: 0; padding: 0;'><thead><th class='gxaHeaderCell'>Adjusted <i>p</i>-value</th>" +
                            (tstatistic !== undefined ? "<th class='gxaHeaderCell'><i>t</i>-statistic</th>" : "") +
                            "<th class='gxaHeaderCell'>Log<sub>2</sub>-fold change</th></thead>" +
                            "<tbody><tr><td style='padding:6px'>" + React.renderComponentToStaticMarkup(formatScientificNotation(pValue)) + "</td>" +
                            (tstatistic !== undefined ? "<td style='padding:6px'>" + tstatistic + "</td>" : "") +
                            "<td style='padding:6px'>" + foldChange + "</td></tr></tbody>" +
                            "</table>";
                    }

                    var props = this.props;

                    $(element).attr('title', '').tooltip({
                        open: function (event, ui) {
                            ui.tooltip.css('background', props.color);
                        },

                        tooltipClass:"help-tooltip gxaPvalueTooltipStyling",

                        content:function () {
                            return buildHeatmapCellTooltip(props.pValue, props.tStat, props.foldChange);
                        }
                    });
                }

            });
        })();

        return {
            Heatmap: Heatmap,
            EnsemblLauncher: heatmapConfig.enableEnsemblLauncher ? EnsemblLauncher : undefined
        };
    };

    return {
        buildBaseline: function (heatmapConfig, $prefFormDisplayLevelsInputElement) { return build(TypeEnum.BASELINE, heatmapConfig, new EventEmitter(), $prefFormDisplayLevelsInputElement); },
        buildProteomicsBaseline: function (heatmapConfig, $prefFormDisplayLevelsInputElement) { return build(TypeEnum.PROTEOMICS_BASELINE, heatmapConfig, new EventEmitter(), $prefFormDisplayLevelsInputElement); },
        buildDifferential: function (heatmapConfig, $prefFormDisplayLevelsInputElement) { return build(TypeEnum.DIFFERENTIAL, heatmapConfig, new EventEmitter(), $prefFormDisplayLevelsInputElement); },
        buildMultiExperiment: function (heatmapConfig, $prefFormDisplayLevelsInputElement) { return build(TypeEnum.MULTIEXPERIMENT, heatmapConfig, new EventEmitter(), $prefFormDisplayLevelsInputElement); }
    };

})(jQuery, React, genePropertiesTooltipModule, factorTooltipModule, contrastTooltipModule, helpTooltipsModule, baselineVarianceModule, EventEmitter, Modernizr);