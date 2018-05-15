package com.smartpesa.smartpesa.flavour;

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.smartpesa.smartpesa.persistence.MerchantComponent;

import java.util.List;

import dagger.Component;

@FlavourScope
@Component(
        modules = {FlavourModule.class},
        dependencies = {MerchantComponent.class}
)
public interface FlavourComponent {

    @FlavourSpecific
    List<IDrawerItem> provideFlavourSpecificDrawerItem();

    @FlavourSpecific
    MenuHandler provideMenuHandler();

    @FlavourSpecific
    PaymentHandler providePaymentHandler();

}
