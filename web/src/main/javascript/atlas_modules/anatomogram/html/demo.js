"use strict";

//*------------------------------------------------------------------*

var React = require('react');
var ReactDOM = require('react-dom');

//*------------------------------------------------------------------*

var Anatomogram = require('../src/Anatomogram.jsx');

var EventEmitter = require('events');

//*------------------------------------------------------------------*

var anatomogramData =
{
    "maleAnatomogramFile": "mouse_male.svg",
    "toggleButtonMaleImageTemplate": "/resources/images/male",
    "femaleAnatomogramFile": "mouse_female.svg",
    "toggleButtonFemaleImageTemplate": "/resources/images/female",
    "brainAnatomogramFile": "mouse_brain.svg",
    "toggleButtonBrainImageTemplate": "/resources/images/brain",
    "allSvgPathIds": ["UBERON_0000947", "UBERON_0001981", "UBERON_0001348", "UBERON_0001347", "EFO_0000962", "UBERON_0000945", "UBERON_0002114", "UBERON_0001264", "UBERON_0002106", "CL_0000336", "UBERON_0002113", "UBERON_0001155", "UBERON_0002108", "UBERON_0001153", "UBERON_0002115", "UBERON_0002116", "UBERON_0001043", "UBERON_0002110", "UBERON_0001831", "UBERON_0001736", "UBERON_0000029", "CL_0000446", "UBERON_0001723", "UBERON_0001211", "UBERON_0002367", "UBERON_0001000", "UBERON_0001301", "UBERON_0000473", "UBERON_0000998", "UBERON_0000989", "UBERON_0001255", "UBERON_0002370", "UBERON_0000981", "UBERON_0002371", "EFO_0000949", "UBERON_0001377", "UBERON_0002240", "UBERON_0002048", "UBERON_0001103", "UBERON_0000010", "UBERON_0003126", "UBERON_0002103", "UBERON_0001645", "UBERON_0000970", "UBERON_0001322", "UBERON_0001242", "UBERON_0002107", "UBERON_0000948", "UBERON_0000955", "UBERON_0014892", "UBERON_0001896", "UBERON_0000956", "UBERON_0000369", "UBERON_0001894", "UBERON_0000007", "EFO_0000530", "UBERON_0002037", "UBERON_0002298", "UBERON_0001891", "UBERON_0001897", "UBERON_0001898", "UBERON_0000004"],
};

var profileRows = [
    {
        "name": "Profile name",
        "expressions": [
            {
                "factorName": "heart",
                "value": "1",
                "svgPathId": "UBERON_0000948"
            },
            {
                "factorName": "hippocampus",
                "value": "1",
                "svgPathId": "EFO_0000530"
            },
            {
                "factorName": "liver",
                "value": "1",
                "svgPathId": "UBERON_0002107"
            },
            {
                "factorName": "lung",
                "value": "1",
                "svgPathId": "UBERON_0002048"
            },
            {
                "factorName": "spleen",
                "value": "1",
                "svgPathId": "UBERON_0002106"
            },
            {
                "factorName": "thymus",
                "value": "1",
                "svgPathId": "UBERON_0002370"
            }
        ]
    }
];

module.exports = function(mountNode) {
    ReactDOM.render(
        React.createElement(Anatomogram, {
            anatomogramData: anatomogramData,
            expressedTissueColour: "red",
            hoveredTissueColour: "indigo",
            profileRows: profileRows,
            eventEmitter: new EventEmitter(),
            atlasBaseURL: "https://www.ebi.ac.uk/gxa"
        }), mountNode
    );
};
