import "./vue.js"
import "./jQuery.js"
import "./vuex.js"

Vue.use(Vuex);

const vue = new Vue({
    el: '#app',
    data: {
        imageData: null,
        errLabelText: null,
        downloadTime: null,
        dbMsg: null,
        searchInputErrorMsg: null,
        databaseEntries: [],
        searchResult: [],
        isDownload: false,
        isSearch: false,
        isDatabase: false,
        validDownloadResponse: false,
        searchDone: false
    },

    methods: {
        getImageData: function () {
            this.errLabelText = null;
            const pidText = document.getElementById("pid").value;
            const cookie = document.getElementById("cookie").value;
            if (pidText.length === 0) {
                this.errLabelText = 'Error: Pixiv id should not be empty!';
                return
            }

            if (isNaN(parseInt(pidText))) {
                this.errLabelText = 'Error: Pixiv id should only include digits!';
                return
            }

            if (cookie.length === 0) {
                this.errLabelText = 'Error: Login cookie should not be empty!';
                return
            }

            const self = this;
            $.ajax({
                type: 'PUT',
                contentType: 'application/hal+json',
                url: 'http://uzuki.me:114/services/download/' + pidText,
                data: JSON.stringify({'cookie' : cookie}),
                success: function (response, textStatus, xhr) {
                    $("#downloadResult").show();
                    this.imageData = JSON.parse(JSON.stringify(response));
                    store.commit('storeImageData', JSON.stringify(response));
                    self.downloadTime = 'Download Time: ' + this.imageData.downloadTime;
                    document.getElementById('image').src = 'data:image/' + this.imageData.imgFormat + ';base64, ' + this.imageData.imageBase64;
                    document.getElementById("localBtn").href = 'data:image/' + this.imageData.imgFormat + ';base64, ' + this.imageData.imageBase64;
                    document.getElementById("localBtn").download = this.imageData.pid + '.' + this.imageData.imgFormat;
                },
                error: function(xhr, textStatus, errorThrown) {
                    self.errLabelText = 'Error: Response http code ' + xhr.status + '. Unable to get image, please check pixiv id and cookie';
                }
            });
        },

        featureSelect: function (id) {
            if (id === 'download') {
                this.isDownload = true;
                this.isSearch = false;
                this.isDatabase = false;
            } else if (id === 'search') {
                this.isDownload = false;
                this.isSearch = true;
                this.isDatabase = false;
            } else if(id === 'database') {
                this.isDownload = false;
                this.isSearch = false;
                this.isDatabase = true;
            }
        },

        setDatabaseBtn: function () {
            if (store.state.imageData == null) {
                return;
            }
            const self = this;
            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: 'http://uzuki.me:114/database/add',
                data: store.state.imageData,
                success: function (response, textStatus, xhr) {
                    console.log(response);
                    self.dbMsg = 'Saved to database!';
                    document.getElementById("dbMsg").style.color = 'black';
                },
                error: function(xhr, textStatus, errorThrown) {
                    console.log('Status: ' + textStatus);
                    console.log('Error: ' + errorThrown);
                    self.dbMsg = 'Error when connecting to database, error code: ' + xhr.status;
                    document.getElementById("dbMsg").style.color = 'red';
                }
            });
        },

        getSource: function () {
            const file = document.getElementById('imageFile').files[0];
            const url = document.getElementById('imgUrl').value;
            if (url !== '') {
                if(file) {
                    this.searchInputErrorMsg = 'Enter one source of file only!'
                    return;
                }

            } else if (file) {
                if(url !== '') {
                    this.searchInputErrorMsg = 'Enter one source of file only!'
                    return;
                }

                const fd = new FormData();
                fd.append('imageFile', file);
                const self = this;
                $.ajax({
                    type: 'POST',
                    enctype: 'multipart/form-data',
                    contentType: false,
                    processData: false,
                    cache: false,
                    url: 'http://uzuki.me:114/services/search',
                    data: fd,
                    success: function (response, textStatus, xhr) {
                        console.log(response);
                        self.searchResult = JSON.parse(JSON.stringify(response))._embedded.sauceNaoResultList;
                        self.searchDone = true;
                    },
                    error: function(xhr, textStatus, errorThrown) {
                        console.log('Status: ' + textStatus);
                        console.log('Error: ' + errorThrown);

                    }
                });
            }


        }
    }
});

const store = new Vuex.Store({
    state: {
        imageData: null
    },
    mutations: {
        storeImageData (state, data) {
            state.imageData = data;
        }
    }
})