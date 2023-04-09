import "./vue.js"
import "./jQuery.js"
import "./vuex.js"

Vue.use(Vuex);

const vue = new Vue({
    el: '#app',
    data: {
        loginMsg: null,
        acctName: null,
        referer: null,
        imageData: null,
        errLabelText: null,
        downloadTime: null,
        dbMsg: null,
        searchInputErrorMsg: null,
        fileType: null,
        databaseEntries: [],
        searchResult: [],
        showLogin: false,
        isRegister: false,
        isLogin: true,
        isLoggedin: false,
        loginStatus: true,
        isDownload: false,
        isSearch: false,
        isDatabase: false,
        downloadLoader: false,
        searchLoader: false,
        databaseLoader: false,
        validDownloadResponse: false,
        selectedSearchImage: false,
        searchDone: false,
        fetchDone: false,
        noData: false
    },

    methods: {
        login: function (referer) {
            this.showLogin = true;
            this.referer = referer;
        },

        closeLoginPopup: function () {
            this.showLogin = false;
        },

        toggleRegister: function() {
            this.isRegister = !this.isRegister;
            this.isLogin = !this.isLogin;
            this.loginMsg = '';
        },

        logout: function () {
            this.acctName = '';
            store.commit('storeAccount', '');
            this.isLoggedin = false;
            this.reload();
        },

        signIn: function () {
            const uname = document.getElementById('acctName').value;
            const password = document.getElementById('password').value;
            this.loginMsg = '';

            if(uname === '') {
                this.loginMsg = 'Account name should not be empty!';
                return;
            }
            if(password === '') {
                this.loginMsg = 'Password should not be empty!';
                return;
            }
            this.verifyAccount(uname, password, 'login');
        },

        register: function () {
            const uname = document.getElementById('acctName').value;
            const password = document.getElementById('password').value;
            const rePassword = document.getElementById('re-password').value;
            this.loginMsg = '';

            if(uname === '') {
                this.loginMsg = 'Account name should not be empty!';
                return;
            }

            if(password === '') {
                this.loginMsg = 'Password should not be empty!';
                return;
            }

            if(password.length < 5) {
                this.loginMsg = 'Password must contains 5 characters!';
                return;
            }

            if(!/[A-Z]/.test(password)) {
                this.loginMsg = 'Password must contains one upper case character!';
                return;
            }

            if(password !== rePassword) {
                this.loginMsg = 'Password inputs are not match!';
                return;
            }

            this.verifyAccount(uname, password, 'register');


        },

        getImageData: function () {
            this.errLabelText = null;
            const pidText = document.getElementById("pid").value;
            if (pidText.length === 0) {
                this.errLabelText = 'Error: Pixiv id should not be empty!';
                return
            }

            if (isNaN(parseInt(pidText))) {
                this.errLabelText = 'Error: Pixiv id should only include digits!';
                return
            }

            this.downloadLoader = true;
            const self = this;
            $.ajax({
                type: 'GET',
                contentType: 'application/hal+json',
                url: 'http://uzuki.me:114/services/download/' + pidText,
                success: function (response, textStatus, xhr) {
                    $("#downloadResult").show();
                    this.imageData = JSON.parse(JSON.stringify(response));
                    store.commit('storeImageData', JSON.stringify(response));
                    self.downloadTime = 'Download Time: ' + this.imageData.downloadTime;
                    document.getElementById('image').src = 'data:image/' + this.imageData.imgFormat + ';base64, ' + this.imageData.imageBase64;
                    document.getElementById("localBtn").href = 'data:image/' + this.imageData.imgFormat + ';base64, ' + this.imageData.imageBase64;
                    document.getElementById("localBtn").download = this.imageData.pid + '.' + this.imageData.imgFormat;
                    self.downloadLoader = false;
                },
                error: function(xhr, textStatus, errorThrown) {
                    self.errLabelText = 'Error: Response http code ' + xhr.status + '. Unable to get image, please check the pixiv id';
                }
            });
        },

        featureSelect: function (id) {
            this.reload();
            if (id === 'downloadFeatureBtn') {
                this.isDownload = true;
                this.isSearch = false;
                this.isDatabase = false;
            } else if (id === 'searchFeatureBtn') {
                this.isDownload = false;
                this.isSearch = true;
                this.isDatabase = false;
            } else if(id === 'databaseFeatureBtn') {
                this.isDownload = false;
                this.isSearch = false;
                this.isDatabase = true;
                this.fetchAllData();
            }

        },

        setDatabaseBtn: function () {
            if (store.state.imageData == null) {
                return;
            }

            if (store.state.account === '' || store.state.account === null) {
                window.scroll(0,0);
                this.login('add');
                return;
            }

            const self = this;
            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: 'http://uzuki.me:114/database/add',
                data: JSON.stringify({'uname' :store.state.account, 'image' : store.state.imageData}),
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

        showSelectedImage: function (id) {
            if (id === 'imageFile'){
                if(document.getElementById('imageFile').files && document.getElementById('imageFile').files[0]) {
                    this.searchInputErrorMsg = '';
                    this.selectedSearchImage = true;
                    document.getElementById('imgUrl').value = '';
                    this.fileType = 'file'
                    const reader = new FileReader();

                    reader.onload = function (e) {
                        $('#searchImage')
                            .attr('src', e.target.result);
                    };

                    reader.readAsDataURL(document.getElementById('imageFile').files[0]);
                } else {
                    this.selectedSearchImage = false;
                }
            } else if (id === 'imgUrl') {
                if(document.getElementById('imgUrl').value !== '') {
                    document.getElementById('imageFile').value = null;
                    this.searchInputErrorMsg = '';
                    this.selectedSearchImage = true;
                    this.fileType = 'url'
                    try {
                        document.getElementById('searchImage').src = document.getElementById('imgUrl').value;
                    } catch (error) {
                        this.selectedSearchImage = false;
                        this.searchInputErrorMsg = 'Invalid image url provided!'
                    }

                } else {
                    this.selectedSearchImage = false;
                }
            }
        },

        getSource: function () {
            const file = document.getElementById('imageFile').files[0];
            const url = document.getElementById('imgUrl').value;
            this.searchLoader = true;
            if (this.fileType === 'url') {
                const self = this;
                $.ajax({
                    type: 'POST',
                    contentType: 'application/json',
                    url: 'http://uzuki.me:114/services/search',
                    data: {"imgUrl" : url},
                    success: function (response, textStatus, xhr) {
                        console.log(response);
                        self.searchResult = JSON.parse(JSON.stringify(response))._embedded.sauceNaoResultList;
                        self.searchDone = true;
                        self.searchLoader = false;
                    },
                    error: function(xhr, textStatus, errorThrown) {
                        console.log('Status: ' + xhr.status);
                        console.log('Error: ' + errorThrown);

                    }
                });
            } else if (this.fileType === 'file') {
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
                        self.searchLoader = false;
                    },
                    error: function(xhr, textStatus, errorThrown) {
                        console.log('Status: ' + xhr.status);
                        console.log('Error: ' + errorThrown);

                    }
                });
            }
        },

        fetchAllData: function () {
            if (store.state.account === '' || store.state.account === null) {
                this.login('fetch');
                return;
            }

            this.databaseLoader = true;
            const self = this;
            $.ajax({
                type: 'POST',
                contentType: 'application/hal+json',
                url: 'http://uzuki.me:114/database/images',
                data: JSON.stringify({'uname' : store.state.account}),
                success: function (response, testStatus, xhr) {
                    try {
                        self.databaseEntries = JSON.parse(JSON.stringify(response))._embedded.pixivImageList;
                    } catch (e) {
                        self.fetchDone = false;
                        self.noData = true;
                        self.databaseLoader = false;
                        return;
                    }

                    self.noData = false;
                    self.fetchDone = true;
                    self.databaseLoader = false;
                },
                error: function (xhr, testStatus, errorThrown) {
                    console.log('Status: ' + xhr.status);
                    console.log('Error: ' + errorThrown);
                }
            });
        },

        deleteRecord: function (pid) {
            const self = this;
            $.ajax({
                type: 'DELETE',
                contentType: 'application/hal+json',
                url: 'http://uzuki.me:114/database/image/' + pid,
                data: store.state.account,
                success: function (response, testStatus, xhr) {
                    try {
                        self.databaseEntries = JSON.parse(JSON.stringify(response))._embedded.pixivImageList;
                    } catch (e) {
                        self.fetchDone = false;
                        self.noData = true;
                        return;
                    }
                    self.noData = false;
                    self.fetchDone = true;
                },
                error: function (xhr, testStatus, errorThrown) {
                    console.log('Status: ' + xhr.status);
                    console.log('Error: ' + errorThrown);
                }
            });
        },

        verifyAccount: function(uname, password, mode) {
            const self = this;
            $.ajax({
                type: 'POST',
                contentType: 'application/json',
                url: 'http://uzuki.me:114/account/' + mode,
                data: JSON.stringify({'uname' : uname, 'password' : password}),
                success: function (response, textStatus, xhr) {
                    const json = JSON.parse(JSON.stringify(response));
                    if(json.error === false) {
                        self.acctName = json.uname;
                        store.commit('storeAccount', json.uname);
                        self.isLoggedin = true;
                        document.getElementById('acctName').value = '';
                        document.getElementById('password').value = '';
                        document.getElementById('re-password').value = '';
                        self.loginMsg = '';
                        self.showLogin = false;
                        self.loginStatus = false;

                        if(self.referer === 'add') {
                            self.setDatabaseBtn();
                        } else if (self.referer === 'fetch') {
                            self.fetchAllData();
                        }

                    } else {
                        self.loginMsg = json.message;
                    }

                },
                error: function(xhr, textStatus, errorThrown) {
                    self.loginMsg = 'Register service error: Code ' + xhr.status
                }
            });
        },

        reload: function () {
            this.downloadLoader = false;
            this.searchLoader = false;
            this.databaseLoader = false;
            this.validDownloadResponse = false;
            this.selectedSearchImage = false;
            this.searchDone = false;
            this.fetchDone = false;
            this.noData = false;
            this.isRegister = false;
            this.isLogin = true;
            this.databaseEntries = [];
            this.searchResult = [];
            this.referer = null;
            this.imageData = null;
            this.errLabelText = null;
            this.downloadTime = null;
            this.dbMsg = null;
            this.searchInputErrorMsg = null;
            $("#downloadResult").hide();
            document.getElementById('imgUrl').value = '';
            document.getElementById('imageFile').value = null;
            document.getElementById("pid").value = '';
            store.commit('storeImageData', '');
        }
    }
});

document.getElementById('app').getElementsByTagName('div').namedItem('search').getElementsByTagName('input').namedItem('imageUrl').addEventListener('input', function (e) {
    const supportedType = ['jpg', 'jpeg', 'png', 'gif', 'webp', 'bmp'];
    const url = e.target.value;
    if(supportedType.includes(url.substring(url.lastIndexOf('.') + 1)) || e.target.value.includes(';base64,')){
        vue.showSelectedImage('imgUrl');
    }
});

const store = new Vuex.Store({
    state: {
        imageData: null,
        account: null
    },
    mutations: {
        storeImageData (state, data) {
            state.imageData = data;
        },
        storeAccount(state, data) {
            state.account = data;
        }
    }
});