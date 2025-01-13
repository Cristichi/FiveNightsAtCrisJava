package es.cristichi.fnac.obj.cams;

import es.cristichi.fnac.exception.ResourceException;

/**
 * Objects for this class creates CameraMaps, as to be able to keep track of different maps for campaign and
 * Custom Nights.
 */
public abstract class CameraMapFactory {
    /**
     * @return Name of the map, for the map selector in the Custom Night menu.
     */
    public abstract String name();
    /**
     * @return A new copy of the CameraMap that represents this factory.
     * @throws ResourceException If any resources could not load.
     */
    public abstract CameraMap generate() throws ResourceException;
    
    @Override
    public String toString() {
        return name();
    }
}
