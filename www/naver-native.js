/**
 * Created by ghyeok on 2017. 7. 25..
 */
var exec = require('cordova/exec');

var Naver = {
    login: function (s, f) {
        exec(s, f, 'Naver', 'login', []);
    },

    logout: function (s, f) {
        exec(s, f, 'Naver', 'logout', []);
    },

    logoutAndDeleteToken: function (s, f) {
        exec(s, f, 'Naver', 'logoutAndDeleteToken', []);
    },

    refreshAccessToken: function (s, f) {
        exec(s, f, 'Naver', 'refreshAccessToken', []);
    },

    getState: function (s, f) {
        exec(s, f, 'Naver', 'getState', []);
    },

    requestApi: function (url, s, f) {
        exec(s, f, 'Naver', 'requestApi', [url]);
    },

    requestMe: function (s, f) {
        exec(s, f, 'Naver', 'requestMe', []);
    }

};

module.exports = Naver;