"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var ReactDOM = require('react-dom');
var ReactDOMServer = require('react-dom/server');
var $ = require('jquery');
require('jquery-ui-bundle');

//*------------------------------------------------------------------*

var NumberFormat = require('number-format');

//*------------------------------------------------------------------*

var CellDifferential = React.createClass({

    propTypes: {
        fontSize: React.PropTypes.number,
        colour: React.PropTypes.string,
        foldChange: React.PropTypes.string,
        pValue: React.PropTypes.string,
        tStat: React.PropTypes.string,
        displayLevels: React.PropTypes.bool.isRequired
    },

    _hasValue: function () {
        return (this.props.foldChange !== undefined);
    },

    _getStyle: function() {
        var style = {};
        if (this.props.fontSize) {
            style.fontSize = this.props.fontSize + "px";
        }

        return style;
    },

    render: function () {
        if (!this._hasValue()) {
            return (<td></td>);
        }

        return (
            <td style={{backgroundColor: this.props.colour, verticalAlign: "middle"}}>
                <div style={this._getStyle()} className={this.props.displayLevels ? "gxaShowCell" : "gxaHideCell"}>
                    {this.props.foldChange}
                </div>
            </td>
        );
    },

    componentDidMount: function () {
        if (this._hasValue()) {
            this._initTooltip(ReactDOM.findDOMNode(this));
        }
    },

    _initTooltip: function(element) {

        //TODO - build this from a React component, like we do for FactorTooltip
        function buildHeatmapCellTooltip (pValue, tstatistic, foldChange) {

            return "<table class='gxaTableGrid' style='margin: 0; padding: 0;'>" +
                       "<thead>" +
                           (pValue !== undefined ?
                               "<th class='gxaHeaderCell'>Adjusted <i>p</i>-value</th>" : "") +
                           (tstatistic !== undefined ?
                               "<th class='gxaHeaderCell'><i>t</i>-statistic</th>" : "") +
                           "<th class='gxaHeaderCell'>Log<sub>2</sub>-fold change</th>" +
                       "</thead>" +
                       "<tbody>" +
                           "<tr>" +
                               (pValue !== undefined ?
                                   "<td style='padding:6px'>" + ReactDOMServer.renderToStaticMarkup(NumberFormat.scientificNotation(pValue)) + "</td>" : "") +
                               (tstatistic !== undefined ?
                                   "<td style='padding:6px'>" + tstatistic + "</td>" : "") +
                               "<td style='padding:6px'>" + foldChange + "</td>" +
                           "</tr>" +
                       "</tbody>" +
                   "</table>";
        }

        var props = this.props;

        $(element).attr('title', '').tooltip({
            open: function (event, ui) {
                ui.tooltip.css('background', props.colour);
            },

            tooltipClass:"gxaHelpTooltip gxaPValueTooltipStyling",

            content:function () {
                return buildHeatmapCellTooltip(props.pValue, props.tStat, props.foldChange);
            }
        });
    }
});

//*------------------------------------------------------------------*

module.exports = CellDifferential;