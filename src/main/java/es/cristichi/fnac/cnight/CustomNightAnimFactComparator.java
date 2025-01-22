package es.cristichi.fnac.cnight;

import es.cristichi.fnac.anim.AnimatronicDrawing;

import java.util.Comparator;

/**
 * Comparator that orders the Custom Night menu by name.
 */
public class CustomNightAnimFactComparator implements Comparator<CustomNightAnimFactory<? extends AnimatronicDrawing>> {
    @Override
    public int compare(CustomNightAnimFactory o1, CustomNightAnimFactory o2) {
        return o1.getNameId().compareTo(o2.getNameId());
    }
}
