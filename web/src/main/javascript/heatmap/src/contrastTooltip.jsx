"use strict";

//*------------------------------------------------------------------*

var React = require('react');

//*------------------------------------------------------------------*

var ContrastTooltip = React.createClass({

    propertyRow: function (property) {
        if (!property.testValue && !property.referenceValue) {
            return null;
        }

        function isFactor(property) {
            return property.contrastPropertyType === 'FACTOR';
        }

        var style = {'whiteSpace': 'normal'};

        if (isFactor(property)) {
            style['fontWeight'] = 'bold';
        } else {
            style['color'] = 'gray';
        }

        return (
            <tr key={property.propertyName}>
                <td style={style}>{property.propertyName}</td>
                <td style={style}>{property.testValue}</td>
                <td style={style}>{property.referenceValue}</td>
            </tr>
        );
    },

    render: function () {
        return (
            <div>
                <div id="contrastExperimentDescription" style={{'fontWeight':'bold', 'color':'blue', 'textAlign': 'center'}}>{this.props.experimentDescription}</div>
                <div id="contrastDescription" style={{'textAlign': 'center'}}>{this.props.contrastDescription}</div>
                <table className="gxaTableGrid" style={{padding: '0px', margin: '0px', width: '100%'}}>
                    <thead>
                        <tr>
                            <th className='gxaHeaderCell'>
                            Property
                            </th>
                            <th className='gxaHeaderCell'>
                            Test value (N={this.props.testReplicates})
                            </th>
                            <th className='gxaHeaderCell'>
                            Reference value (N={this.props.referenceReplicates})
                            </th>
                        </tr>
                    </thead>
                    <tbody>
                        {this.props.properties.map(this.propertyRow)}
                    </tbody>
                </table>
            </div>
        );
    }

});

//*------------------------------------------------------------------*

module.exports = ContrastTooltip;