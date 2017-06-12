    var exec = require('cordova/exec');
    var PLUGIN_NAME = "ftp";
    module.exports = {
        /**
         * Upload a file to a FTP server.
         *
         * @param file      The file to be uploaded to the ftp server.
         * @param url       The url for the ftp server.
         * @param success   The success callback.
         * @param failure   The failure callback.
         */
        upload: function (url, indexRecord, success, failure) {
            return exec(success, failure, PLUGIN_NAME, "upload", [url, indexRecord]);
        },

        /**
         * Download a ascii file from a FTP server.
         *
         * @param file      The ascii file to be downloaded from the ftp server.
         * @param url       The url for the ftp server.
         * @param success   The success callback.
         * @param failure   The failure callback. 
         */
        downloadAsciiFile: function (file, url, success, failure) {
            return exec(success, failure, PLUGIN_NAME, "downloadAsciiFile", [file, url]);
        },

        /**
         * Download a binary file from a FTP server.
         *
         * @param file      The binary file to be downloaded from the ftp server.
         * @param url       The url for the ftp server.
         * @param success   The success callback.
         * @param failure   The failure callback.
         */
        downloadBinaryFile: function (nameFileDownload, fileDownload, success, failure) {
            return exec(success, failure, PLUGIN_NAME, "downloadBinaryFile", [nameFileDownload]);
        },

        /**
         * Download a ascii file as an ascii string from a FTP server.
         *
         * @param url       The url for the ftp server.
         * @param success   The success callback.
         * @param failure   The failure callback.
         */
        downloadAsciiString: function (url, success, failure) {
            return exec(success, failure, PLUGIN_NAME, "downloadAsciiString", ["", url]);
        },
        upFile: function (success, failure) {
            return exec(success, failure, PLUGIN_NAME, "upFile", [url, ""]);
        },
		connectWifi: function (ssid,password,success, failure) {
                    return exec(success, failure, PLUGIN_NAME, "connectWifi", [ssid,password]);
        },
        deleteFile: function (path,success, failure) {
            return exec(success, failure, PLUGIN_NAME, "deleteFile", [path]);
        },
        download: function (config, success, failure) {
            config = config || {};

            if (typeof (config.FTPServer) === 'undefined') { //0
                config.FTPServer = "";
            }
            if (typeof (config.FTPUser) === 'undefined') { //1
                config.FTPUser = "";
            }
            if (typeof (config.FTPPass) === 'undefined') { //2
                config.FTPPass = "";
            }
            if (typeof (config.FTPPort) === 'undefined') { //3
                config.FTPPort = "";
            }
            if (typeof (config.FTPFolder) === 'undefined') { //4
                config.FTPFolder = "";
            }
            if (typeof (config.Folder) === 'undefined') { //5
                config.Folder = "";
            }
            if (typeof (config.FileName) === 'undefined') { //6
                config.FileName = "";
            }
            return exec(success, failure, PLUGIN_NAME, "download", [config.FTPServer, config.FTPUser, config.FTPPass, config.FTPPort, config.FTPFolder, config.Folder, config.FileName]);
        },
        uploadProcess: function (success, failure) {
            return exec(success, failure, PLUGIN_NAME, "uploadProcess", []);
        },
        postGateInProcess: function (success, failure) {
            return exec(success, failure, PLUGIN_NAME, "postGateInProcess", []);
        },
        startUpload: function (success, failure) {
            return exec(success, failure, PLUGIN_NAME, "startUpload", []);
        },
        stopUpload: function (success, failure) {
            return exec(success, failure, PLUGIN_NAME, "stopUpload", []);
        },
        evenUpload: function (success, failure) {
            return exec(success, failure, PLUGIN_NAME, "stopUpload", []);
        }
    };


