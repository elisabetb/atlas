"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var ReactDOM = require('react-dom');
var EventEmitter = require('wolfy87-eventemitter');

//*------------------------------------------------------------------*

var Anatomogram = require('./src/Anatomogram.jsx');

function AnatomogramBuilder(domNode, anatomogramData, profileRows, expressedColour, hoverColour, eventEmitter) {

    ReactDOM.render(
        React.createElement(
            Anatomogram,
            {
                anatomogramData: anatomogramData,
                expressedTissueColour: expressedColour,
                hoveredTissueColour: hoverColour,
                atlasBaseURL: "http://www.ebi.ac.uk/gxa",
                profileRows: profileRows,
                eventEmitter: eventEmitter
            }
        ),
        domNode
    );
}

//*------------------------------------------------------------------*

exports.AnatomogramBuilder = AnatomogramBuilder;
exports.EventEmitter = EventEmitter;
