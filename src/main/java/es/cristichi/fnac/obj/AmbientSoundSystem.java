package es.cristichi.fnac.obj;

import es.cristichi.fnac.obj.cams.Camera;
import es.cristichi.fnac.obj.cams.CameraMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AmbientSoundSystem extends ArrayList<AmbientSound> {
    private final int tickInterval;

    public AmbientSoundSystem(int tickInterval, AmbientSound... sounds){
        super(List.of(sounds));
        this.tickInterval = tickInterval;
    }

    public void attemptRandomSound(Random rng, int tick, CameraMap map){
        AmbientSound sound = get(rng.nextInt(size()));
        if (tick%tickInterval==0){
            if (sound.shouldPlayOnCams()){
                ArrayList<Camera> values = new ArrayList<>(map.values());
                sound.attemptPlay(rng, values.get(rng.nextInt(values.size())));
            } else {
                sound.attemptPlay(rng);
            }
        }
    }
}
