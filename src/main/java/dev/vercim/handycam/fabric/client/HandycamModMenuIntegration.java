package dev.vercim.handycam.fabric.client;

//? fabric {

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class HandycamModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return HandycamConfigScreen::create;
    }
}
//?}
