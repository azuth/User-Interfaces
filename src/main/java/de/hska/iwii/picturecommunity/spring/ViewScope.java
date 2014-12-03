package de.hska.iwii.picturecommunity.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import javax.faces.context.FacesContext;
import java.util.Map;

public class ViewScope implements Scope {
    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        if (FacesContext.getCurrentInstance().getViewRoot() != null) {
            Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();

            if (viewMap.containsKey(name)) {
                return viewMap.get(name);
            } else {
                Object o = objectFactory.getObject();

                viewMap.put(name, o);
                return o;
            }
        }
        return null;
    }

    @Override
    public Object remove(String name) {
        if (FacesContext.getCurrentInstance().getViewRoot() != null) {
            return FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(name);
        }
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }
    
    @Override
    public String getConversationId() {
        return null;
    }
}
