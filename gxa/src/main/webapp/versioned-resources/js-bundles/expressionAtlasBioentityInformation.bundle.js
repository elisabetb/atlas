var expressionAtlasBioentityInformation=webpackJsonp_name_([3],{0:/*!******************************************************!*\
  !*** ./atlas_bundles/bioentity-information/index.js ***!
  \******************************************************/
function(e,t,r){"use strict";e.exports=r(/*! ./src/renderer.js */3004)},3004:/*!*************************************************************!*\
  !*** ./atlas_bundles/bioentity-information/src/renderer.js ***!
  \*************************************************************/
function(e,t,r){"use strict";var n=r(/*! react */2),o=r(/*! react-dom */31),a=r(/*! ./BioentityInformation.jsx */3005);t.render=function(e){o.render(n.createElement(a,{bioentityProperties:e.payload}),"string"==typeof e.target?document.getElementById(e.target):e.target)}},3005:/*!**************************************************************************!*\
  !*** ./atlas_bundles/bioentity-information/src/BioentityInformation.jsx ***!
  \**************************************************************************/
function(e,t,r){"use strict";var n=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var r=arguments[t];for(var n in r)Object.prototype.hasOwnProperty.call(r,n)&&(e[n]=r[n])}return e},o=r(/*! react */2);r(/*! ./BioentityInformation.css */3006);var a={text:o.PropTypes.string.isRequired,url:o.PropTypes.string.isRequired,relevance:o.PropTypes.number.isRequired},i={type:o.PropTypes.string.isRequired,name:o.PropTypes.string.isRequired,values:o.PropTypes.arrayOf(o.PropTypes.shape(a)).isRequired},s={bioentityProperties:o.PropTypes.arrayOf(o.PropTypes.shape(i))},p=o.createClass({displayName:"BioentityProperty",propTypes:i,getInitialState:function(){return{showAll:!1}},_pickMostRelevant:function(e){var t=e.map(function(e){return e.relevance}).sort(function(e,t){return t-e}).concat([0,0,0])[e.size<3?e.size-1:2];return e.filter(function(e){return e.relevance>=t})},_renderProperty:function(e,t){return e.url?o.createElement("a",{key:e.url+" "+t,className:"bioEntityCardLink",href:e.url,target:"_blank"},e.text):o.createElement("span",{key:e.text+" "+t},e.text)},_zipWithCommaSpans:function(e){return[].concat.apply([],e.map(function(e,t){return[e,o.createElement("span",{key:"comma "+t},", ")]})).slice(0,-1)},render:function(){var e=this.props.values.length-this._pickMostRelevant(this.props.values).length,t=["go","po"].indexOf(this.props.type)>-1&&e>0;return o.createElement("tr",null,o.createElement("td",{className:"gxaBioentityInformationCardPropertyType"},this.props.name),o.createElement("td",null,o.createElement("div",null,t?o.createElement("span",null,this._zipWithCommaSpans((this.state.showAll?this.props.values:this._pickMostRelevant(this.props.values)).sort(function(e,t){return t.relevance===e.relevance?t.text.toLowerCase()<e.text.toLowerCase()?1:-1:t.relevance-e.relevance}).map(this._renderProperty)),o.createElement("a",{role:"button",style:{cursor:"pointer"},onClick:function(){this.setState(function(e){return{showAll:!e.showAll}})}.bind(this)},this.state.showAll?" (show less)":" … and "+e+" more")):this._zipWithCommaSpans(this.props.values.map(this._renderProperty)))))}}),l=o.createClass({displayName:"BioentityInformation",propTypes:s,render:function(){return o.createElement("div",{className:"gxaBioentityInformationCard"},o.createElement("table",null,o.createElement("tbody",null,this.props.bioentityProperties.map(function(e){return o.createElement(p,n({key:e.type},e))}))))}});e.exports=l},3006:/*!**************************************************************************!*\
  !*** ./atlas_bundles/bioentity-information/src/BioentityInformation.css ***!
  \**************************************************************************/
function(e,t,r){var n=r(/*! !./../~/css-loader!./BioentityInformation.css */3007);"string"==typeof n&&(n=[[e.id,n,""]]);r(/*! ./../~/style-loader/addStyles.js */3009)(n,{});n.locals&&(e.exports=n.locals)},3007:/*!*****************************************************************************************************************************!*\
  !*** ./atlas_bundles/bioentity-information/~/css-loader!./atlas_bundles/bioentity-information/src/BioentityInformation.css ***!
  \*****************************************************************************************************************************/
function(e,t,r){t=e.exports=r(/*! ./../~/css-loader/lib/css-base.js */3008)(),t.push([e.id,".gxaBioentityInformationCard{margin-top:20px}.gxaBioentityInformationCard table{margin:0;width:auto;border:none}.gxaBioentityInformationCard td{border:none}td.gxaBioentityInformationCardPropertyType{font-size:14px;font-weight:700;white-space:nowrap;padding-right:2em;border:none}.gxaBioentityInformationCardPropertyValue{border:none}",""])},3008:/*!**************************************************************************!*\
  !*** ./atlas_bundles/bioentity-information/~/css-loader/lib/css-base.js ***!
  \**************************************************************************/
480,3009:/*!*************************************************************************!*\
  !*** ./atlas_bundles/bioentity-information/~/style-loader/addStyles.js ***!
  \*************************************************************************/
481});