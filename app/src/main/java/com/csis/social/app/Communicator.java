package com.csis.social.app;

import java.util.Date;

public interface Communicator {
    interface AddQuizFragment {
        void onDeadlineChoosed(Date dateClicked);
    }
}