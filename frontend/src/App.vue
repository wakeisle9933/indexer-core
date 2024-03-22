<template>
  <div id="app">
    <h1>Google Quick Search Indexer by <a href="#" @click="goToMyGithub">My Codegate</a></h1>
    <div>
      <label for="sitemapInput">
        Input Sitemap
        <span v-if="currentSitemap">(Current Sitemap: {{ currentSitemap }})</span>
      </label>
      <input type="text" id="sitemapInput" placeholder="ex) https://sample.com/sitemap.xml" v-model="sitemapUrl" @keyup.enter="addSitemap">
      <button @click="addSitemap">Add</button>
      <button class="send-api-button" @click="sendAPI" :disabled="!canSendAPI">Send API</button>
    </div>
    <div class="container">
      <div class="sitemap-list-container">
        <h2 class="sticky-url-head">Sitemap URLs</h2>
        <button @click="copyUrls">{{ copyButtonText }}</button>
        <ul>
          <li v-for="(sitemap, index) in sitemapUrls" :key="index">{{ sitemap }}</li>
        </ul>
      </div>
      <div class="processed-list-container">
        <h2 class="sticky-url-head">Processed URLs</h2>
        <button @click="openProcessedFile">Open</button>
        <ul>
          <li v-for="(url, index) in processedUrls" :key="index">{{ url }}</li>
        </ul>
      </div>
      <div class="exception-list-container">
        <h2 class="sticky-url-head">Exception URLs</h2>
        <button @click="openExceptionFile">Open</button>
        <ul>
          <li v-for="(url, index) in exceptionUrls" :key="index">{{ url }}</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      previewUrl: '',
      sitemapUrl: '',
      currentSitemap: '',
      sitemapUrls: [],
      processedUrls: [],
      exceptionUrls: [],
      copyButtonText: 'Copy',
      copyMessageTimeout: null
    };
  },
  computed: {
    canSendAPI() {
      return this.sitemapUrls.length > 0;
    }
  },
  methods: {
    async addSitemap() {
      if (this.sitemapUrl.trim() !== '') {
        if (this.isValidDomain(this.sitemapUrl) && this.hasSitemapXml(this.sitemapUrl)) {
          try {
            const response = await axios.post('api/sitemaps', {url: this.sitemapUrl});
            this.sitemapUrls = response.data.sitemapUrls;
            this.processedUrls = response.data.processedUrls;
            this.exceptionUrls = response.data.exceptionUrls;
            this.currentSitemap = this.sitemapUrl;
            this.sitemapUrl = '';
          } catch (error) {
            console.error(error);
          }
        } else {
          alert("Check the URL you entered (example: https://mycodegate.com/sitemap.xml)")
        }
      } else {
        alert("you didn't entered URL")
      }
    },
    isValidDomain(url) {
      const pattern = new RegExp('^(https?:\\/\\/)?'+ // protocol
          '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|'+ // domain name
          '((\\d{1,3}\\.){3}\\d{1,3}))'+ // OR ip (v4) address
          '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*'+ // port and path
          '(\\?[;&a-z\\d%_.~+=-]*)?'+ // query string
          '(\\#[-a-z\\d_]*)?$','i'); // fragment locator
      return !!pattern.test(url);
    },
    hasSitemapXml(url) {
      return url.toLowerCase().endsWith('/sitemap.xml');
    },
    goToMyGithub() {
      window.location.href = 'https://github.com/wakeisle9933';
    },
    async openProcessedFile() {
      try {
        if (!this.currentSitemap) {
          alert("Add your sitemap first!");
          return;
        }
        await axios.get('/api/sitemaps/files/processed', {
          params: { sitemapUrl: this.currentSitemap }
        });
      } catch (error) {
        console.error(error);
      }
    },
    async openExceptionFile() {
      try {
        if (!this.currentSitemap) {
          alert("Add your sitemap first!");
          return;
        }
        await axios.get('api/sitemaps/files/exception', {
          params: { sitemapUrl: this.currentSitemap }
        });
      } catch (error) {
        console.error(error);
      }
    },
    copyUrls() {
      if (this.sitemapUrls.length === 0) {
        alert(`There's nothing to copy!`);
        return;
      }

      const urlsText = this.sitemapUrls.join('\n');
      navigator.clipboard.writeText(urlsText)
      .then(() => {
        this.copyButtonText = 'Copy Completed';
        clearTimeout(this.copyMessageTimeout);
        this.copyMessageTimeout = setTimeout(() => {
          this.copyButtonText = 'Copy';
        }, 500);
      })
      .catch((error) => {
        console.error('copy fail:', error);
      });
    },
    sendAPI() {
      // Send API 로직 구현 - 작성중
    }
  }
};
</script>

<style>
#app {
  font-family: Arial, sans-serif;
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

label {
  display: block;
  margin-bottom: 10px;
}

input[type="text"] {
  width: 70%;
  padding: 5px;
  margin-right: 10px;
}

button {
  padding: 5px 10px;
}

ul {
  list-style-type: none;
  padding: 0;
}

li {
  margin-bottom: 5px;
}

.container {
  display: flex;
  justify-content: space-between;
}

.sitemap-list-container,
.processed-list-container,
.exception-list-container {
  width: 33%;
  max-height: 300px;
  overflow-y: auto;
  transition: max-height 0.3s ease;
}

.sticky-url-head {
  position: sticky;
  top: 0;
  background-color: white;
  padding: 10px 0;
  margin: 0;
  z-index: 1;
}

.send-api-button {
  background-color: #ff0000;
  color: #ffffff;
  border: none;
  padding: 10px 20px;
  margin-left: 10px;
  font-size: 16px;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  box-shadow: 0 0 5px rgba(0, 0, 0, 0.3);
  transition: background-color 0.3s;
}

.send-api-button:hover {
  background-color: #cc0000;
}

.send-api-button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  box-shadow: none;
}

</style>