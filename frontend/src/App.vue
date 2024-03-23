<template>
  <div id="app">
    <h1>Google Quick Search Indexer by <a href="#" @click="goToMyGithub">My Codegate</a></h1>
    <div>
      <label for="sitemapInput">
        Insert Sitemap
        <span v-if="currentSitemap">(Current Sitemap: {{ currentSitemap }} - {{ sitemapAddedTime }})
            <span
                :class="{ 'refresh-text': currentSitemap }"
                @click="addSitemap"
            >Refresh</span>
        </span>
      </label>
      <input type="text" id="sitemapInput" placeholder="ex) https://sample.com/sitemap.xml" v-model="sitemapUrl" @keyup.enter="addSitemap">
      <button @click="addSitemap">Add</button>
      <button class="send-api-button" @click="sendAPI" :disabled="!canSendAPI || loading">
        {{ loading ? 'Indexing...' : 'Send API' }}
      </button>
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
      </div>
    </div>
    <div class="container">
      <div class="sitemap-list-container">
        <h2 class="sticky-url-head">Target URLs ({{ sitemapUrls.length }})</h2>
        <button @click="copyUrls">{{ copyButtonText }}</button>
        <ul>
          <li v-for="(sitemap, index) in sitemapUrls" :key="index">
            <a :href="sitemap" target="_blank">{{ sitemap }}</a>
          </li>
        </ul>
      </div>
      <div class="processed-list-container">
        <h2 class="sticky-url-head">Indexed URLs ({{ processedUrls.length }})</h2>
        <button @click="openProcessedFile">Edit</button>
        <ul>
          <li v-for="(url, index) in processedUrls" :key="index">
            <a :href="url" target="_blank">{{ url }}</a>
          </li>
        </ul>
      </div>
      <div class="exception-list-container">
        <h2 class="sticky-url-head">Except URLs ({{ exceptionUrls.length }})</h2>
        <button @click="openExceptionFile">Edit</button>
        <ul>
          <li v-for="(url, index) in exceptionUrls" :key="index">
            <a :href="url" target="_blank">{{ url }}</a>
          </li>
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
      copyMessageTimeout: null,
      sitemapAddedTime: '',
      loading: false
    };
  },
  computed: {
    canSendAPI() {
      return this.sitemapUrls.length > 0;
    }
  },
  methods: {
    async addSitemap() {
      if(this.currentSitemap !== '') {
        this.sitemapUrl = this.currentSitemap;
      }

      if (this.sitemapUrl.trim() !== '') {
        if (this.isValidDomain(this.sitemapUrl) && this.hasSitemapXml(this.sitemapUrl)) {
          try {
            const response = await axios.post('api/sitemaps', {url: this.sitemapUrl});
            this.sitemapUrls = response.data.sitemapUrls;
            this.processedUrls = response.data.processedUrls;
            this.exceptionUrls = response.data.exceptionUrls;
            this.currentSitemap = this.sitemapUrl;
            this.sitemapAddedTime = this.getCurrentTime();
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
      window.location.href = 'https://github.com/wakeisle9933/indexer-core';
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
    async sendAPI() {
      try {
        if (this.sitemapUrls.length === 0) {
          alert("don't have a sitemap URL to send! 😅");
          return;
        }

        this.loading = true;

        const response = await axios.post('api/sitemaps/indexing', {
          urls: this.sitemapUrls,
          processedUrls: this.processedUrls,
          exceptionUrls: this.exceptionUrls,
          sitemapUrl: this.currentSitemap
        });

        if(response.data.success === true) {
          alert(response.data.count + " URLs have been indexed 😆🚀");
          await this.addSitemap();
        }

        console.log(response);
      } catch (error) {
        console.error(error);
      } finally {
        this.loading = false;
      }
    },
    getCurrentTime() {
      const now = new Date();
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      return `${hours}:${minutes}:${seconds}`;
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
  max-height: 400px;
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

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

</style>

<style scoped>
.refresh-text {
  color: blue;
  cursor: pointer;
  font-weight: bold;
}
</style>