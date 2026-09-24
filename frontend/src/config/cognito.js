import { Amplify } from 'aws-amplify';

const redirectUri = import.meta.env.VITE_COGNITO_REDIRECT_URI;

Amplify.configure({
    Auth: {
        Cognito: {
            userPoolId: import.meta.env.VITE_COGNITO_USER_POOL_ID,
            userPoolClientId: import.meta.env.VITE_COGNITO_CLIENT_ID,

            loginWith: {
                oauth: {
                    domain: import.meta.env.VITE_COGNITO_DOMAIN,

                    scopes: [
                        'openid',
                        'email',
                        'profile',
                        'salfa360-api/access_as_user',
                    ],

                    redirectSignIn: [redirectUri],
                    redirectSignOut: [redirectUri],

                    responseType: 'code',
                },
            },
        },
    },
});