#!/bin/bash
# @author: rpetry
# @date:   8 Feb 2013

# This script measures web response times on a given hostname for selected Baseline Atlas queries

if [ $# -lt 1 ]; then
        echo "Usage: $0 HOSTNAME"
        exit;
fi

HOSTNAME=$1
ATLAS_ROOT_URL="http://${HOSTNAME}/gxa"

echo "Web response times (secs) for ${ATLAS_ROOT_URL}:"
echo "--------------------------------------------------------------------------------------------------------------------------------------------------------------"
echo "query                                                                                          http_code    time_connect    time_starttransfer    time_total"
curl -o /dev/null -X GET -s -w %{http_code}:%{time_connect}:%{time_starttransfer}:%{time_total} "${ATLAS_ROOT_URL}/experiments/E-GEOD-30352" | awk -F":" '{print "/experiments/E-GEOD-30352                                                                      "$1"          "$2"           "$3"                 "$4}'
curl -o /dev/null -X GET -s -w %{http_code}:%{time_connect}:%{time_starttransfer}:%{time_total} "${ATLAS_ROOT_URL}/experiments" | awk -F":" '{print "/experiments                                                                                   "$1"          "$2"           "$3"                 "$4}'
curl -o /dev/null -X GET -s -w %{http_code}:%{time_connect}:%{time_starttransfer}:%{time_total} "${ATLAS_ROOT_URL}/proteins/ENSP00000355434" | awk -F":" '{print "/proteins/ENSP00000355434                                                                      "$1"          "$2"           "$3"                 "$4}'
curl -o /dev/null -X GET -s -w %{http_code}:%{time_connect}:%{time_starttransfer}:%{time_total} "${ATLAS_ROOT_URL}/genes/AT5G20250" | awk -F":" '{print "/genes/AT5G20250                                                                               "$1"          "$2"           "$3"                 "$4}'
curl -o /dev/null -X GET -s -w %{http_code}:%{time_connect}:%{time_starttransfer}:%{time_total} "${ATLAS_ROOT_URL}/widgets/heatmap/protein?geneQuery=REACT_111102&geneSetMatch=true" | awk -F":" '{print "/widgets/heatmap/protein?geneQuery=REACT_111102&geneSetMatch=true                              "$1"          "$2"           "$3"                 "$4}'
curl -o /dev/null -X GET -s -w %{http_code}:%{time_connect}:%{time_starttransfer}:%{time_total} "${ATLAS_ROOT_URL}/widgets/heatmap/protein?geneQuery=ENSG00000187003+ENSG00000185264&propertyType=identifier" | awk -F":" '{print "/widgets/heatmap/protein?geneQuery=ENSG00000187003+ENSG00000185264&propertyType=identifier     "$1"          "$2"           "$3"                 "$4}'
echo ""
echo ""
