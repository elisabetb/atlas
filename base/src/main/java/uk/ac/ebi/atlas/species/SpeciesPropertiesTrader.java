package uk.ac.ebi.atlas.species;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Named
public class SpeciesPropertiesTrader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpeciesPropertiesTrader.class);

    private SpeciesPropertiesDao speciesPropertiesDao;
    private ImmutableSortedMap<String, SpeciesProperties> nameToSpecies = ImmutableSortedMap.of();

    @Inject
    public void setSpeciesPropertiesDao(SpeciesPropertiesDao speciesPropertiesDao) {
        this.speciesPropertiesDao = speciesPropertiesDao;
    }

    @PostConstruct
    public String refresh() throws IOException {
        try {
            LOGGER.info("Reading species properties...");

            ImmutableSortedMap<String, SpeciesProperties> newMap = buildMap();
            MapDifference<String, SpeciesProperties> diff = Maps.difference(nameToSpecies, newMap);
            nameToSpecies = newMap;

            LOGGER.info("Retrieved {} species properties", nameToSpecies.size());
            return describeMapDifference(diff);

        } catch (IOException e) {
            LOGGER.error("Error reading species properties");
            throw e;
        }
    }

    private String describeMapDifference(MapDifference<?, ?> mapDifference) {
        if (mapDifference.areEqual()) {
            return "No changes were made to the reference species";
        } else {
            String updatedMessage = mapDifference.entriesDiffering().isEmpty() ?
                    null : Arrays.deepToString(mapDifference.entriesDiffering().keySet().toArray()) + " updated";

            String removedMessage = mapDifference.entriesOnlyOnLeft().isEmpty() ?
                    null : Arrays.deepToString(mapDifference.entriesOnlyOnLeft().keySet().toArray()) + " removed";

            String addedMessage = mapDifference.entriesOnlyOnRight().isEmpty() ?
                    null : Arrays.deepToString(mapDifference.entriesOnlyOnRight().keySet().toArray()) + " added";

            return Joiner.on("; ").skipNulls().join(updatedMessage, removedMessage, addedMessage);
        }
    }

    private ImmutableSortedMap<String, SpeciesProperties> buildMap() throws IOException {
        ImmutableSortedMap.Builder<String, SpeciesProperties> nameToSpeciesPropertiesBuilder =
                ImmutableSortedMap.naturalOrder();

        for (SpeciesProperties speciesProperties : speciesPropertiesDao.fetchAll()) {
            nameToSpeciesPropertiesBuilder.put(speciesProperties.referenceName(), speciesProperties);
        }

        return nameToSpeciesPropertiesBuilder.build();
    }

    public SpeciesProperties get(String speciesName) {
        return StringUtils.isNotEmpty(speciesName) && nameToSpecies.containsKey(normalise(speciesName)) ?
                nameToSpecies.get(normalise(speciesName)) : SpeciesProperties.UNKNOWN;
    }

    public Collection<SpeciesProperties> getAll() {
        return nameToSpecies.values();
    }

    private String normalise(String str) {
        String normalisedString = str.contains("_") ? str.replace("_", " ") : str;

        return Joiner.on(' ').skipNulls().join(Arrays.copyOf(normalisedString.toLowerCase().split(" "), 2));
    }

}