package com.smartpesa.smartpesa.flavour;

import com.smartpesa.smartpesa.fragment.payment.SmartPesaSaleFragment;
import com.smartpesa.smartpesa.models.SmartPesaTransactionType;
import com.smartpesa.smartpesa.persistence.DefaultFromAccount;
import com.smartpesa.smartpesa.persistence.DefaultToAccount;
import com.smartpesa.smartpesa.persistence.MerchantModule;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import javax.inject.Inject;

public class SmartPesaMenuHandler extends MenuHandler {

    int fromAccount, toAccount;

    @Inject
    public SmartPesaMenuHandler(@DefaultFromAccount int defFromAccount, @DefaultToAccount int defToAccount) {
        super(defFromAccount, defToAccount);
        this.fromAccount = defFromAccount;
        this.toAccount = defToAccount;
    }

    @Nullable
    @Override
    public Fragment fragmentForMenuWithId(int identifier) {
        if (identifier == MerchantModule.MENU_ID_SALE) {
            return SmartPesaSaleFragment.newInstance(SmartPesaTransactionType.SALE, fromAccount, toAccount);
        }else {
            return super.fragmentForMenuWithId(identifier);
        }
    }
}
