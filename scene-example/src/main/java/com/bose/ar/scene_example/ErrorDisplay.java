package com.bose.ar.scene_example;

//
//  ErrorDisplay.java
//  BoseWearable
//
//  Created by Tambet Ingo on 01/09/2019.
//  Copyright © 2019 Bose Corporation. All rights reserved.
//

import androidx.annotation.NonNull;

public interface ErrorDisplay {
    void showWarning(@NonNull String message);
    void showError(@NonNull String message);
}
